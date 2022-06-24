package com.devyoung.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobListener implements JobExecutionListener {

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		long time = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
		System.out.println("total execute time >> " + time);
	}

}
