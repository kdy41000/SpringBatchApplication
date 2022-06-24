package com.devyoung.batch.chunk.writer;

import java.nio.file.Path;
import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.devyoung.batch.domain.ApiRequestVO;
import com.devyoung.batch.domain.ApiResponseVO;
import com.devyoung.service.AbstractApiService;

public class ApiItemWriter1 extends FlatFileItemWriter<ApiRequestVO> {

	private final AbstractApiService apiService;
	
	public ApiItemWriter1(AbstractApiService apiService) {
		this.apiService = apiService;
	}
	
	@Override
	public void write(List<? extends ApiRequestVO> items) throws Exception {
		ApiResponseVO responseVO = apiService.service(items);
		System.out.println("responseVO = " + responseVO);
		
		items.forEach(item -> item.setApiResponseVO(responseVO));
		
		super.setResource(new FileSystemResource("C:\\workspace_springsecurity\\springbatch\\src\\main\\resources\\product1.txt"));  // 파일 저장
		super.open(new ExecutionContext());  // 쓸 객체
		super.setLineAggregator(new DelimitedLineAggregator<>());
		super.setAppendAllowed(true);  // 이어쓰기
		super.write(items);
	}

}
