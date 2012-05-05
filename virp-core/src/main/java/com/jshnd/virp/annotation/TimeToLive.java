package com.jshnd.virp.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TimeToLive {

	public static final int NONE = -1;

	int seconds();

}
