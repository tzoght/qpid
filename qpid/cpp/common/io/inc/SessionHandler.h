/*
 *
 * Copyright (c) 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#ifndef _SessionHandler_
#define _SessionHandler_

#include "InputHandler.h"
#include "InitiationHandler.h"
#include "ProtocolInitiation.h"
#include "TimeoutHandler.h"

namespace qpid {
namespace io {

    class SessionHandler : public virtual qpid::framing::InitiationHandler,
        public virtual qpid::framing::InputHandler, 
        public virtual TimeoutHandler
    {
    public:
        virtual void closed() = 0;
	virtual ~SessionHandler(){}
    };

}
}


#endif
