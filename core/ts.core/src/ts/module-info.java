open module typescript.java.core {
	requires transitive com.google.gson;
	requires transitive minimal.json;
	requires transitive org.osgi.core;
	requires transitive org.tukaani.xz;
//	exports com.google.gson.annotations;
//	exports com.google.gson.reflect;
//	exports com.google.gson.stream;
	exports ts;
}