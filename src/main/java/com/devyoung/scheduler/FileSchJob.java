package com.devyoung.scheduler;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class FileSchJob extends QuartzJobBean {

	@Autowired
	private Job fileJob;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private JobExplorer jobExplorer; 
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		String requestDate = (String)context.getJobDetail().getJobDataMap().get("requestDate");  // 20220610
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("id", new Date().getTime())  // 중복 실행되도 실행가능하도록 unique하지 않은 id값 설정
				.addString("requestDate", requestDate)
				.toJobParameters();
		
		
			// 이미 존재하는 requestDate가 인자값으로 들어오면 batch job이 실행되지 않고 exception 발생
			try {
				int jobInstanceCount = jobExplorer.getJobInstanceCount(fileJob.getName());
				List<JobInstance> jobInstances = jobExplorer.getJobInstances(fileJob.getName(), 0, jobInstanceCount);
				
				if(jobInstances.size() > 0) {
					for(JobInstance jobInstance : jobInstances) {
						List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
						List<JobExecution> jobExecutionsList =  jobExecutions.stream()
								.filter(jobExecution -> 
											jobExecution.getJobParameters().getString("requestDate").equals(requestDate))
								.collect(Collectors.toList());
						if(jobExecutionsList.size() > 0) {
							throw new JobExecutionException(requestDate + " already exists");
						}
					}
				}
			} catch (NoSuchJobException e1) {
				e1.printStackTrace();
			}
			
		
		try {
			jobLauncher.run(fileJob, jobParameters);
		} catch (JobExecutionAlreadyRunningException e) {
			e.printStackTrace();
		} catch (JobRestartException e) {
			e.printStackTrace();
		} catch (JobInstanceAlreadyCompleteException e) {
			e.printStackTrace();
		} catch (JobParametersInvalidException e) {
			e.printStackTrace();
		}
	}

}