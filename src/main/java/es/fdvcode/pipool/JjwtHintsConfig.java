package es.fdvcode.pipool;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
//@ImportRuntimeHints(JjwtHints.class)
class JjwtHintsConfig {}

//class JjwtHints implements RuntimeHintsRegistrar {
//	  @Override
//	  public void registerHints(RuntimeHints hints, ClassLoader cl) {
//
//		var refl = hints.reflection();
//		  
//	    // === Clases de jjwt-impl instanciadas por reflexión ===
//		refl.registerType(
//	        TypeReference.of("io.jsonwebtoken.impl.security.KeysBridge"),
//	        MemberCategory.INVOKE_PUBLIC_METHODS,
//	        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
//	        MemberCategory.DECLARED_FIELDS
//	    );
//
//		refl.registerType(
//	        TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder"),
//	        MemberCategory.INVOKE_PUBLIC_METHODS,
//	        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
//	    );
//
//		refl.registerType(
//	        TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder"),
//	        MemberCategory.INVOKE_PUBLIC_METHODS,
//	        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
//	    );
//
//		refl.registerType(
//	        TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParser"),
//	        MemberCategory.INVOKE_PUBLIC_METHODS,
//	        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
//	    );
//
//	    // (Opcional, pero útil si usas Claims por defecto)
//		refl.registerType(
//	        TypeReference.of("io.jsonwebtoken.impl.DefaultClaims"),
//	        MemberCategory.INVOKE_PUBLIC_METHODS,
//	        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
//	        MemberCategory.DECLARED_FIELDS
//	    );
//	    
//	    // NUEVOS: builder/headers y sus inner classes $Supplier
//	    String[] extra = {
//	      "io.jsonwebtoken.impl.DefaultJwtBuilder$Supplier",
//	      "io.jsonwebtoken.impl.DefaultJwtHeaderBuilder",
//	      "io.jsonwebtoken.impl.DefaultJwtHeaderBuilder$Supplier",
//	      "io.jsonwebtoken.impl.DefaultJwsHeader",
//	      "io.jsonwebtoken.impl.DefaultHeader",
//	      "io.jsonwebtoken.impl.DefaultClaims" // útil si manipulas Claims por defecto
//	    };
//	    for (String cn : extra) {
//	      refl.registerType(TypeReference.of(cn),
//	        MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS);
//	    }	    
//
//	    // === META-INF/services usados por jjwt-jackson ===
//	    // Para que el ServiceLoader encuentre Serializer/Deserializer JSON
//	    hints.resources().registerPattern("META-INF/services/io.jsonwebtoken.io.Serializer");
//	    hints.resources().registerPattern("META-INF/services/io.jsonwebtoken.io.Deserializer");
//
//	     //(Opcional) Si quieres ser explícito con las clases de Jackson de JJWT:
//	    refl.registerType(
//	         TypeReference.of("io.jsonwebtoken.jackson.io.JacksonSerializer"),
//	         MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
//	    refl.registerType(
//	         TypeReference.of("io.jsonwebtoken.jackson.io.JacksonDeserializer"),
//	         MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
//	  
//	  // <-- NUEVO: la clase interna que te está faltando
//	    refl.registerType(
//	         TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder$Supplier"),
//	         MemberCategory.INVOKE_PUBLIC_METHODS,
//	         MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
//	         MemberCategory.DECLARED_FIELDS
//	     );
//
//	     // Recomendado también (por si tu flujo de parseo lo usa de forma similar):
//	    refl.registerType(
//	         TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder$Supplier"),
//	         MemberCategory.INVOKE_PUBLIC_METHODS,
//	         MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
//	         MemberCategory.DECLARED_FIELDS
//	     );
//	  }
//	}

