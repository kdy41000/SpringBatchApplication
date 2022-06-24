package com.devyoung.batch.job.file;

import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


import com.devyoung.batch.chunk.processor.FileItemProcessor;
import com.devyoung.batch.domain.Product;
import com.devyoung.batch.domain.ProductVO;

@Configuration
@RequiredArgsConstructor
public class FileJobConfiguration {

	   private final JobBuilderFactory jobBuilderFactory;
	   private final StepBuilderFactory stepBuilderFactory;
	   private final EntityManagerFactory entityManagerFactory;

	    @Bean
	    public Job fileJob() {
	        return jobBuilderFactory.get("fileJob")
	        		.incrementer(new RunIdIncrementer())
	                .start(fileStep1())
	                .build();
	    }

	    @Bean
	    public Step fileStep1() {
	        return stepBuilderFactory.get("fileStep1")
	                .<ProductVO, Product>chunk(10)
	                .reader(fileItemReader(null))
	                .processor(fileItemProcessor())
	                 .writer(fileItemWriter())
	                .build();
	    }

	   @Bean
	   @StepScope
	    public FlatFileItemReader<ProductVO> fileItemReader(@Value("#{jobParameters['requestDate']}") String requestDate) {
		   	System.out.println("=== fileItemReader ===");
	    	System.out.println("requestDate:" + requestDate);
	        return new FlatFileItemReaderBuilder<ProductVO>()
	                .name("flatFile")
	                .resource(new ClassPathResource("product_" + requestDate +".csv"))
	               // .resource(new ClassPathResource("product_20220610.csv"))
	                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
	                .targetType(ProductVO.class)
	                .linesToSkip(1)
	                .delimited().delimiter(",")
	                .names("id","name","price","type")
	                .build();
	    }

	    @Bean
	    public ItemProcessor<ProductVO, Product> fileItemProcessor() {
	        System.out.println("== fileItemProcessor ==");
	    	return new FileItemProcessor();
	    }

	    @Bean
	    public JpaItemWriter<Product> fileItemWriter() {
	        return new JpaItemWriterBuilder<Product>()
	                .entityManagerFactory(entityManagerFactory)
	                .usePersist(true)
	                .build();
	    }

}
