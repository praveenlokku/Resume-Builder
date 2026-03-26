package com.resumebuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ResumeBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeBuilderApplication.class, args);
	}

	@Bean
	public CommandLineRunner schemaUpdate(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Manually adding the column in case Hibernate 'update' missed it
				jdbcTemplate.execute("ALTER TABLE resumes ADD COLUMN IF NOT EXISTS archived BOOLEAN DEFAULT FALSE;");
				System.out.println("✅ Schema synchronized: 'archived' column ensured.");
			} catch (Exception e) {
				System.out.println("⚠️ Schema update skipped or failed: " + e.getMessage());
			}
		};
	}
}
