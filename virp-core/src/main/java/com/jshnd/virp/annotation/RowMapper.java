package com.jshnd.virp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RowMapper {

	String columnFamily();

	TimeToLive defaultTimeToLive() default @TimeToLive(seconds = TimeToLive.NONE);

}
