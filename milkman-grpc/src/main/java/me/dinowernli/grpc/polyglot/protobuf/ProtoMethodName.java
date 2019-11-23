package me.dinowernli.grpc.polyglot.protobuf;

import com.google.common.base.Joiner;

/** Represents a full grpc method, including package, service and method names. */
public class ProtoMethodName {
  // TODO(dino): Use autovalue for this class.
  private final String packageName;
  private final String serviceName;
  private final String methodName;

  /** Parses a string of the form"<package>.<service>/<method>". */
  public static ProtoMethodName parseFullGrpcMethodName(String fullMethodName) {
    // Ask grpc for the service name.
    String fullServiceName = io.grpc.MethodDescriptor.extractFullServiceName(fullMethodName);
    if (fullServiceName == null) {
      throw new IllegalArgumentException("Could not extract full service from " + fullMethodName);
    }

    // Make sure there is a '/' and use the rest as the method name.
    int serviceLength = fullServiceName.length();
    if (serviceLength + 1 >= fullMethodName.length()
        || fullMethodName.charAt(serviceLength) != '/') {
      throw new IllegalArgumentException("Could not extract method name from " + fullMethodName);
    }
    String methodName = fullMethodName.substring(fullServiceName.length() + 1);

    // Extract the leading package from the service name.
    int index = fullServiceName.lastIndexOf('.');
    if (index == -1) {
      throw new IllegalArgumentException("Could not extract package name from " + fullServiceName);
    }
    String packageName = fullServiceName.substring(0, index);

    // Make sure there is a '.' and use the rest as the service name.
    if (index + 1 >= fullServiceName.length() || fullServiceName.charAt(index) != '.') {
      throw new IllegalArgumentException("Could not extract service from " + fullServiceName);
    }
    String serviceName = fullServiceName.substring(index + 1);

    return new ProtoMethodName(packageName, serviceName, methodName);
  }

  private ProtoMethodName(String packageName, String serviceName, String methodName) {
    this.packageName = packageName;
    this.serviceName = serviceName;
    this.methodName = methodName;
  }

  /** Returns the full package name of the method. */
  public String getPackageName() {
    return packageName;
  }

  /** Returns the (unqualified) service name of the method. */
  public String getServiceName() {
    return serviceName;
  }

  /** Returns the fully qualified service name of the method. */
  public String getFullServiceName() {
    return Joiner.on(".").join(packageName, serviceName);
  }

  /** Returns the (unqualified) method name of the method. */
  public String getMethodName() {
    return methodName;
  }
}
