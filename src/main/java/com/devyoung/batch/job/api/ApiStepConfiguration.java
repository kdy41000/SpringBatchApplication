package com.devyoung.batch.job.api;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.devyoung.batch.chunk.processor.ApiItemProcessor1;
import com.devyoung.batch.chunk.processor.ApiItemProcessor2;
import com.devyoung.batch.chunk.processor.ApiItemProcessor3;
import com.devyoung.batch.chunk.writer.ApiItemWriter1;
import com.devyoung.batch.chunk.writer.ApiItemWriter2;
import com.devyoung.batch.chunk.writer.ApiItemWriter3;
import com.devyoung.batch.classifier.ProcessorClassifier;
import com.devyoung.batch.classifier.WriterClassifier;
import com.devyoung.batch.domain.ApiRequestVO;
import com.devyoung.batch.domain.ProductVO;
import com.devyoung.batch.partition.ProductPartitioner;
import com.devyoung.service.ApiService1;
import com.devyoung.service.ApiService2;
import com.devyoung.service.ApiService3;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApiStepConfiguration {

	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;
	
	private final ApiService1 apiService1;
	private final ApiService2 apiService2;
	private final ApiService3 apiService3;
	
	private int chunkSize = 10;
	
	@Bean
	public Step apiMasterStep() throws Exception {
		return stepBuilderFactory.get("apiMasterStep")
				.partitioner(apiSlaveStep().getName(), partitioner()) // Thread처리
				.step(apiSlaveStep())
				.gridSize(3)
				.taskExecutor(taskExecutor())
				.build();
	}
	
	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(3);
		taskExecutor.setMaxPoolSize(6);
		taskExecutor.setThreadNamePrefix("api-thread-");
		return taskExecutor;
	}
	
	@Bean
	public Step apiSlaveStep() throws Exception {
		return stepBuilderFactory.get("apiSlaveStep")
				.<ProductVO, ProductVO>chunk(chunkSize)
				.reader(itemReader(null))
				.processor(itemProcessor())
				.writer(itemWriter())
				.build();
	}
	
	@Bean
	@StepScope
	public ItemReader<ProductVO> itemReader(@Value("#{stepExecutionContext['product']}") ProductVO productVO) throws Exception {
		JdbcPagingItemReader<ProductVO> reader = new JdbcPagingItemReader<>();
		reader.setDataSource(dataSource);
		reader.setPageSize(chunkSize);
		reader.setRowMapper(new BeanPropertyRowMapper<>(ProductVO.class));
		
		MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
		queryProvider.setSelectClause("id, name, price, type");
		queryProvider.setFromClause("from product");
		queryProvider.setWhereClause("where type = :type");
		
		Map<String, Order> sortKeys = new HashMap<>(1);
		sortKeys.put("id", Order.DESCENDING);
		queryProvider.setSortKeys(sortKeys);
		
		reader.setParameterValues(QueryGenerator.getParameterForQuery("type",productVO.getType()));
		reader.setQueryProvider(queryProvider);
		reader.afterPropertiesSet();
		
		return reader;
	}

	@Bean
	public ItemProcessor itemProcessor() {
		ClassifierCompositeItemProcessor<ProductVO, ApiRequestVO> processor 
							= new ClassifierCompositeItemProcessor<>();
		ProcessorClassifier<ProductVO, ItemProcessor<?, ? extends ApiRequestVO>> classifier = new ProcessorClassifier(); // <class역할을 할 클래스타입, 반환할 객체>
		Map<String, ItemProcessor<ProductVO, ApiRequestVO>> processorMap = new HashMap<>();
		processorMap.put("1", new ApiItemProcessor1());
		processorMap.put("2", new ApiItemProcessor2());
		processorMap.put("3", new ApiItemProcessor3());
		
		classifier.setProcessorMap(processorMap);
		
		processor.setClassifier(classifier);
		
		return processor;
	}

	@Bean
	public ItemWriter itemWriter() {
		ClassifierCompositeItemWriter<ApiRequestVO> writer 
						= new ClassifierCompositeItemWriter<>();
		WriterClassifier<ApiRequestVO, ItemWriter<? super ApiRequestVO>> classifier = new WriterClassifier(); // <class역할을 할 클래스타입, 반환할 객체>
		Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();
		writerMap.put("1", new ApiItemWriter1(apiService1));
		writerMap.put("2", new ApiItemWriter2(apiService2));
		writerMap.put("3", new ApiItemWriter3(apiService3));
		
		classifier.setWriterMap(writerMap);
		
		writer.setClassifier(classifier);
		
		return writer;
	}

	@Bean
	public ProductPartitioner partitioner() {
		ProductPartitioner productPartitioner = new ProductPartitioner();
		productPartitioner.setDataSource(dataSource);
		return productPartitioner;
	}
}
