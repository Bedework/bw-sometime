/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.bedework.sometime.impl;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.bedework.sometime.model.ScheduleOwner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Asynchronous implementation of {@link AvailableScheduleReflectionService}.
 * 
 * Reflect operations are asynchronous and depend on periodic executions of
 * {@link #run()} or {@link #processReflectQueue()}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AsyncAvailableScheduleReflectionServiceImpl.java $
 */
@Service("availableScheduleReflectionService")
public class AsyncAvailableScheduleReflectionServiceImpl extends
		DefaultAvailableScheduleReflectionServiceImpl implements Runnable {

	private final Queue<ScheduleOwner> reflectQueue = new LinkedBlockingDeque<ScheduleOwner>();
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.DefaultAvailableScheduleReflectionServiceImpl#reflectAvailableSchedule(org.jasig.schedassist.model.ScheduleOwner)
	 */
	@Override
	public void reflectAvailableSchedule(ScheduleOwner owner) {
		if (null != owner && !this.reflectQueue.contains(owner) && owner.isReflectSchedule()) {
			// push owner into reflect queue
			boolean added = this.reflectQueue.add(owner);
			if (added) {
				LOG.info("added owner to reflection queue: " + owner);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.DefaultAvailableScheduleReflectionServiceImpl#purgeReflections(org.jasig.schedassist.model.ScheduleOwner, java.util.Date, java.util.Date)
	 */
	@Async
	@Override
	public void purgeReflections(ScheduleOwner owner, Date start, Date end) {
		// remove the owner from the reflection queue if they are present
		this.reflectQueue.remove(owner);
		super.purgeReflections(owner, start, end);
	}

	/**
	 * Walks through the internal queue, calling {@link #processScheduleOwner(ScheduleOwner)}
	 * on each entry.
	 */
	protected void processReflectQueue() {
		// iterate through queue
		while(!this.reflectQueue.isEmpty()) {
			// pop owner from queue
			final ScheduleOwner owner = this.reflectQueue.poll();
			if (owner == null) {
				break;
			}
			boolean success = processScheduleOwner(owner);
			if (!success) {
				LOG.warn("failed to process owner " + owner);
			}
		}
	}
	
	/**
	 * Processes the reflection queue.
	 * 
	 * @see #processReflectQueue()
	 * @see java.lang.Runnable#run()
	 */
	@Scheduled(fixedDelay=60000)
	@Override
	public void run() {
		processReflectQueue();
	}
}
