package milkman.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileSize;
import milkman.PlatformUtil;

public class LogbackConfiguration extends ContextAwareBase implements Configurator {

	private static boolean muteConsole = false;
	
	public static void setMuteConsole(boolean muteConsole) {
		LogbackConfiguration.muteConsole = muteConsole;
	}
	
	@Override
	public void configure(LoggerContext loggerContext) {
		addInfo("Setting up logback configuration.");
		if (!muteConsole) {
			setupConsoleAppender(loggerContext);
		}
		setupFileAppender(loggerContext);
	}

	private void setupConsoleAppender(LoggerContext loggerContext) {
		ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<ILoggingEvent>();
		ca.setContext(loggerContext);
		ca.setName("STDOUT");
		
		var filter = new ThresholdFilter();
		filter.setLevel("INFO");
		filter.start();
		ca.addFilter(filter);
		
		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<ILoggingEvent>();
		encoder.setContext(loggerContext);

		
		// same as
		// PatternLayout layout = new PatternLayout();
		// layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		TTLLLayout layout = new TTLLLayout();

		layout.setContext(loggerContext);
		layout.start();
		encoder.setLayout(layout);

		
		
		ca.setEncoder(encoder);
		ca.start();

		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(ca);
	}
	
	private void setupFileAppender(LoggerContext loggerContext) {
		RollingFileAppender<ILoggingEvent> fa = new RollingFileAppender<ILoggingEvent>();
		fa.setFile(PlatformUtil.getWritableLocationForFile("errors.log"));
		
		SizeBasedTriggeringPolicy<ILoggingEvent> sizePolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
		sizePolicy.setContext(loggerContext);
		sizePolicy.setMaxFileSize(FileSize.valueOf("10mb"));
		sizePolicy.start();
		fa.setTriggeringPolicy(sizePolicy);
		var policy = new FixedWindowRollingPolicy();
		policy.setMaxIndex(3);
		policy.setMinIndex(1);
		policy.setFileNamePattern("errors.%i.log.zip");
		policy.setParent(fa);
		policy.setContext(loggerContext);
		policy.start();
		fa.setRollingPolicy(policy);
		
		
		fa.setContext(loggerContext);
		fa.setName("FILE");
		
		var filter = new ThresholdFilter();
		filter.setLevel("ERROR");
		filter.start();
		fa.addFilter(filter);

		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<ILoggingEvent>();
		encoder.setContext(loggerContext);

		// same as
		// PatternLayout layout = new PatternLayout();
		// layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		PatternLayout layout = new PatternLayout();
		layout.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");

		layout.setContext(loggerContext);
		layout.start();
		encoder.setLayout(layout);
		fa.setEncoder(encoder);

		
		fa.start();

		Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(fa);
	}


}
