package com.devyoung.batch.classifier;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import com.devyoung.batch.domain.ApiRequestVO;

public class WriterClassifier<C, T> implements Classifier<C, T> {

	private Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();
	
	@Override
	public T classify(C classifiable) {
		return (T)writerMap.get(((ApiRequestVO)classifiable).getProductVO().getType());
	}

	public void setWriterMap(Map<String, ItemWriter<ApiRequestVO>> writerMap) {
		this.writerMap = writerMap;
	}

	
}
