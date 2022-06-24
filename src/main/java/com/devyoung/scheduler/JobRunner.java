package com.devyoung.scheduler;

import java.util.Map;

import org.quartz.*;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import static org.quartz.JobBuilder.newJob;

// 스프링 배치가 실행되는 시점에 스케줄러 실행
public abstract class JobRunner implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		doRun(args);
		
	}
	
	protected abstract void doRun(ApplicationArguments args);
	
	public JobDetail buildJobDetail(Class job, String name, String group, Map param) {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.putAll(param);
		
		return newJob(job).withIdentity(name, group)
				.usingJobData(jobDataMap)
				.build();
	}
	
	public Trigger buildJobTrigger(String scheduleExp) {
		return TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp)).build();
	}

}
