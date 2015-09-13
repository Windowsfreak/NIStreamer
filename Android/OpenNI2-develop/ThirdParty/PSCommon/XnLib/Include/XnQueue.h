/*****************************************************************************
*                                                                            *
*  PrimeSense PSCommon Library                                               *
*  Copyright (C) 2012 PrimeSense Ltd.                                        *
*                                                                            *
*  This file is part of PSCommon.                                            *
*                                                                            *
*  Licensed under the Apache License, Version 2.0 (the "License");           *
*  you may not use this file except in compliance with the License.          *
*  You may obtain a copy of the License at                                   *
*                                                                            *
*      http://www.apache.org/licenses/LICENSE-2.0                            *
*                                                                            *
*  Unless required by applicable law or agreed to in writing, software       *
*  distributed under the License is distributed on an "AS IS" BASIS,         *
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
*  See the License for the specific language governing permissions and       *
*  limitations under the License.                                            *
*                                                                            *
*****************************************************************************/
#ifndef _XN_QUEUE_H_
#define _XN_QUEUE_H_

#include "XnList.h"

namespace xnl
{
template <class T, class TAlloc = LinkedNodeDefaultAllocator<T> >
class Queue : protected List<T, TAlloc>
{
public:
	typedef List<T, TAlloc> Base;

	Queue() : Base() {}
	Queue(const Queue& other) : Base()
	{
		*this = other;
	}
	Queue& operator=(const Queue& other)
	{
		Base::operator=(other);
		return *this;
	}
	~Queue() {}

	using typename Base::ConstIterator;
	using Base::IsEmpty;
	using Base::Begin;
	using Base::End;
	using Base::Size;

	XnStatus Push(const T& value)
	{
		return Base::AddLast(value);
	}
	XnStatus Pop(T& value)
	{
		typename Base::Iterator it = Begin();
		if (it == End())
		{
			return XN_STATUS_IS_EMPTY;
		}
		value = *it;
		return Base::Remove(it);
	}

	const T& Top() const
	{
		return *Begin();
	}
	T& Top()
	{
		return *Begin();
	}
};

} // xnl


#endif // _XN_QUEUE_H_
