package com.university.config;

import com.university.mybatis.mapper.FacultyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@ApplicationScoped
public class MyBatisProducers {

    @Inject
    @Named("sqlSessionFactory")
    private SqlSessionFactory sqlSessionFactory;

    @Produces
    @ApplicationScoped
    public FacultyMapper produceFacultyMapper() {
        return createMapperProxy(FacultyMapper.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T createMapperProxy(Class<T> mapperInterface) {
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                new MapperInvocationHandler<>(mapperInterface, sqlSessionFactory)
        );
    }

    private static class MapperInvocationHandler<T> implements InvocationHandler {
        private final Class<T> mapperInterface;
        private final SqlSessionFactory sqlSessionFactory;

        public MapperInvocationHandler(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
            this.mapperInterface = mapperInterface;
            this.sqlSessionFactory = sqlSessionFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try (var sqlSession = sqlSessionFactory.openSession(true)) { // auto-commit enabled
                T mapper = sqlSession.getMapper(mapperInterface);
                return method.invoke(mapper, args);
            }
        }
    }
}