
package com.quartercode.disconnected.server.world.comp.proc.prog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
@Inherited
public @interface ProgramTaskConfiguration {

    Class<? extends ProgramTaskMetadataProvider> metadataProvider ();

}
