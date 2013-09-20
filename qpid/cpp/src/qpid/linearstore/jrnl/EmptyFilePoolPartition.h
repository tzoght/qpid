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

#ifndef QPID_QLS_JRNL_EMPTYFILEPOOLPARTITION_H_
#define QPID_QLS_JRNL_EMPTYFILEPOOLPARTITION_H_

namespace qpid {
namespace qls_jrnl {

    class EmptyFilePoolPartition;

}} // namespace qpid::qls_jrnl

#include "qpid/linearstore/jrnl/EmptyFilePool.h"
#include "qpid/linearstore/jrnl/EmptyFilePoolTypes.h"
#include "qpid/linearstore/jrnl/smutex.h"
#include <string>
#include <map>
#include <vector>

namespace qpid {
namespace qls_jrnl {

class EmptyFilePoolPartition
{
public:
    static const std::string efpTopLevelDir;
protected:
    typedef std::map<efpFileSizeKib_t, EmptyFilePool*> efpMap_t;
    typedef efpMap_t::iterator efpMapItr_t;
    typedef efpMap_t::const_iterator efpMapConstItr_t;

    const efpPartitionNumber_t partitionNum;
    const std::string partitionDir;
    efpMap_t efpMap;
    smutex efpMapMutex;

    void validatePartitionDir();

public:
    EmptyFilePoolPartition(const efpPartitionNumber_t partitionNum_, const std::string& partitionDir_);
    virtual ~EmptyFilePoolPartition();
    void findEmptyFilePools();
    efpPartitionNumber_t partitionNumber() const;
    std::string partitionDirectory() const;

    EmptyFilePool* getEmptyFilePool(const efpFileSizeKib_t efpFileSizeKb);
    void getEmptyFilePoolSizesKb(std::vector<efpFileSizeKib_t>& efpFileSizesKbList) const;
    void getEmptyFilePools(std::vector<EmptyFilePool*>& efpList);
};

}} // namespace qpid::qls_jrnl

#endif /* QPID_QLS_JRNL_EMPTYFILEPOOLPARTITION_H_ */