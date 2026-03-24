package com.chudahuang.srm.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * MyBatis Plus 配置
 * 支持MySQL和PostgreSQL数据库自动识别
 */
@Configuration
public class MybatisPlusConfig {

    @Value("${spring.datasource.druid.driver-class-name:}")
    private String driverClassName;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataSource dataSource) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 根据驱动类名自动检测数据库类型
        DbType dbType = detectDbType(driverClassName);
        if (dbType == null) {
            // 如果通过驱动类名无法检测，尝试通过数据源检测
            dbType = detectDbTypeFromDataSource(dataSource);
        }

        // 默认使用MySQL
        if (dbType == null) {
            dbType = DbType.MYSQL;
        }

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    /**
     * 根据驱动类名检测数据库类型
     */
    private DbType detectDbType(String driverClassName) {
        if (driverClassName == null) {
            return null;
        }

        if (driverClassName.contains("mysql")) {
            return DbType.MYSQL;
        } else if (driverClassName.contains("postgresql")) {
            return DbType.POSTGRE_SQL;
        } else if (driverClassName.contains("oracle")) {
            return DbType.ORACLE;
        } else if (driverClassName.contains("sqlserver")) {
            return DbType.SQL_SERVER;
        } else if (driverClassName.contains("h2")) {
            return DbType.H2;
        }

        return null;
    }

    /**
     * 从数据源检测数据库类型
     */
    private DbType detectDbTypeFromDataSource(DataSource dataSource) {
        try {
            String databaseProductName = dataSource.getConnection().getMetaData().getDatabaseProductName();
            if (databaseProductName != null) {
                String productName = databaseProductName.toLowerCase();
                if (productName.contains("mysql")) {
                    return DbType.MYSQL;
                } else if (productName.contains("postgresql")) {
                    return DbType.POSTGRE_SQL;
                } else if (productName.contains("oracle")) {
                    return DbType.ORACLE;
                } else if (productName.contains("sql server")) {
                    return DbType.SQL_SERVER;
                } else if (productName.contains("h2")) {
                    return DbType.H2;
                }
            }
        } catch (SQLException e) {
            // 检测失败，返回null使用默认值
        }
        return null;
    }
}
