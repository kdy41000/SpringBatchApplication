package com.devyoung.batch.chunk.processor;

import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

import com.devyoung.batch.domain.Product;
import com.devyoung.batch.domain.ProductVO;

public class FileItemProcessor implements ItemProcessor<ProductVO, Product>{

	@Override
	public Product process(ProductVO item) throws Exception {
		System.out.println("=== process ===");
		System.out.println("item : " + item);
		ModelMapper modelMapper = new ModelMapper();
		Product product = modelMapper.map(item, Product.class);
		return product;
	}

}
