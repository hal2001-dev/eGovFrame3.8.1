package egovframework.example.cmm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 두 번째 DB(dataSource2/sqlSession2)에 바인딩할 MyBatis 매퍼임을 표시하는 마커 애노테이션.
 *
 * <p>기본 DB 매퍼는 {@code org.apache.ibatis.annotations.Mapper} 를, 두 번째 DB 매퍼는
 * 이 {@code @SecondaryMapper} 를 붙여 구분합니다. 이렇게 하면 두 개의
 * MapperScannerConfigurer 가 <b>같은 basePackage(egovframework.example)</b> 를 스캔하더라도
 * 애노테이션이 다르므로 서로 겹치지 않고 각자 올바른 SqlSessionFactory 에 바인딩됩니다.</p>
 *
 * <p>패키지로 DS 를 나누기 애매할 때(같은 패키지에 두 DB 매퍼가 섞이는 경우) 유용합니다.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SecondaryMapper {
}
