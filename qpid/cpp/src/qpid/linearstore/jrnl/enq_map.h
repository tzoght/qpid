/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

#ifndef QPID_LEGACYSTORE_JRNL_ENQ_MAP_H
#define QPID_LEGACYSTORE_JRNL_ENQ_MAP_H

namespace qpid
{
namespace qls_jrnl
{
class enq_map;
}}

#include "qpid/linearstore/jrnl/jexception.h"
#include "qpid/linearstore/jrnl/smutex.h"
#include <map>
#include <pthread.h>
#include <vector>

namespace qpid
{
namespace qls_jrnl
{

    /**
    * \class enq_map
    * \brief Class for storing the physical file id (pfid) and a transaction locked flag for each enqueued
    *     data block using the record id (rid) as a key. This is the primary mechanism for
    *     deterimining the enqueue low water mark: if a pfid exists in this map, then there is
    *     at least one still-enqueued record in that file. (The transaction map must also be
    *     clear, however.)
    *
    * Map rids against pfid and lock status. As records are enqueued, they are added to this
    * map, and as they are dequeued, they are removed. An enqueue is locked when a transactional
    * dequeue is pending that has been neither committed nor aborted.
    * <pre>
    *   key      data
    *
    *   rid1 --- [ pfid, txn_lock ]
    *   rid2 --- [ pfid, txn_lock ]
    *   rid3 --- [ pfid, txn_lock ]
    *   ...
    * </pre>
    */
    class enq_map
    {
    public:
        // return/error codes
        static int16_t EMAP_DUP_RID;
        static int16_t EMAP_LOCKED;
        static int16_t EMAP_RID_NOT_FOUND;
        static int16_t EMAP_OK;
        static int16_t EMAP_FALSE;
        static int16_t EMAP_TRUE;

    private:

        struct emap_data_struct
        {
            uint16_t    _pfid;
            bool        _lock;
            emap_data_struct(const uint16_t pfid, const bool lock) : _pfid(pfid), _lock(lock) {}
        };
        typedef std::pair<uint64_t, emap_data_struct> emap_param;
        typedef std::map<uint64_t, emap_data_struct> emap;
        typedef emap::iterator emap_itr;

        emap _map;
        smutex _mutex;
        std::vector<uint32_t> _pfid_enq_cnt;

    public:
        enq_map();
        virtual ~enq_map();

        void set_num_jfiles(const uint16_t num_jfiles);
        inline uint32_t get_enq_cnt(const uint16_t pfid) const { return _pfid_enq_cnt.at(pfid); };

        int16_t insert_pfid(const uint64_t rid, const uint16_t pfid); // 0=ok; -3=duplicate rid;
        int16_t insert_pfid(const uint64_t rid, const uint16_t pfid, const bool locked); // 0=ok; -3=duplicate rid;
        int16_t get_pfid(const uint64_t rid); // >=0=pfid; -1=rid not found; -2=locked
        int16_t get_remove_pfid(const uint64_t rid, const bool txn_flag = false); // >=0=pfid; -1=rid not found; -2=locked
        bool is_enqueued(const uint64_t rid, bool ignore_lock = false);
        int16_t lock(const uint64_t rid); // 0=ok; -1=rid not found
        int16_t unlock(const uint64_t rid); // 0=ok; -1=rid not found
        int16_t is_locked(const uint64_t rid); // 1=true; 0=false; -1=rid not found
        inline void clear() { _map.clear(); }
        inline bool empty() const { return _map.empty(); }
        inline uint32_t size() const { return uint32_t(_map.size()); }
        void rid_list(std::vector<uint64_t>& rv);
        void pfid_list(std::vector<uint16_t>& fv);
    };

}}

#endif // ifndef QPID_LEGACYSTORE_JRNL_ENQ_MAP_H