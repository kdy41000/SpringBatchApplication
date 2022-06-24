package com.devyoung.scheduler;

import java.util.HashMap;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class ApiJobRunner extends JobRunner {

	// quartz scheduler
	@Autowired
	private Scheduler scheduler;
	
	@Override
	protected void doRun(ApplicationArguments args) {
		
		String[] sourceArgs = args.getSourceArgs();  // 어플리케이션 구동 시 설정한 인자값(20220610)
		
		JobDetail jobDetail = buildJobDetail(ApiSchJob.class, "apiJob", "batch", new HashMap());
		Trigger trigger = buildJobTrigger("0/30 * * * * ?");  // 30초마다
		jobDetail.getJobDataMap().put("requestDate", sourceArgs[0]);
		
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
