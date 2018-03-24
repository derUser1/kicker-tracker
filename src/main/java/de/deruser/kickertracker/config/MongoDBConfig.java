package de.deruser.kickertracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import com.mongodb.MongoClient;

/**
 * Custom mongodb configuration to disable "_class" attribute in documents.
 */
@Configuration
public class MongoDBConfig extends AbstractMongoConfiguration {

  //
//  @Value("${spring.data.mongodb.host:#{null}}")
//  private String host;
//
//  @Value("${spring.data.mongodb.port:27017}")
//  private Integer port;

  @Value("${spring.data.mongodb.database:kickertracker}")
  private String dbName;

  @Override
  public MongoClient mongoClient() {
    return new MongoClient();
  }

  @Bean
  @Override
  public MappingMongoConverter mappingMongoConverter() throws Exception {
    MappingMongoConverter mmc = super.mappingMongoConverter();
    mmc.setTypeMapper(new DefaultMongoTypeMapper(null));
    return mmc;
  }

  @Override
  protected String getDatabaseName() {
    return dbName;
  }
}
