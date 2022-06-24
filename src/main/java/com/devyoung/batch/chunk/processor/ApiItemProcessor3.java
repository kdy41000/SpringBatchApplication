package com.devyoung.batch.chunk.processor;

import org.springframework.batch.item.ItemProcessor;

import com.devyoung.batch.domain.ApiRequestVO;
import com.devyoung.batch.domain.ProductVO;

public class ApiItemProcessor3 implements ItemProcessor<ProductVO, ApiRequestVO>{

	@Override
	public ApiRequestVO process(ProductVO item) throws Exception {
		return ApiRequestVO.builder()
				.id(item.getId())
				.productVO(item)
				.build();
	}

}
