package com.codetest.bookingsystem.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatabaseCleaner {

	@PersistenceContext
	private EntityManager entityManager;

	private List<String> tableNames;

	@Transactional
	public void clearTables() {
		if (tableNames == null) {
			tableNames = entityManager.getMetamodel().getEntities().stream()
					.filter(e -> e.getJavaType().getAnnotation(jakarta.persistence.Entity.class) != null).map(e -> {
						String tableName = e.getName(); // Default to entity name
						jakarta.persistence.Table tableAnnotation = e.getJavaType()
								.getAnnotation(jakarta.persistence.Table.class);
						if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
							tableName = tableAnnotation.name();
						}
						return tableName;
					}).collect(Collectors.toList());
		}

		entityManager.flush();
		entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
		tableNames.forEach(tableName -> entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate());
		entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
	}
}