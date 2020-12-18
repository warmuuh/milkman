package milkman.plugin.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.*;
import milkman.plugin.test.domain.TestAspect.TestDetails;
import milkman.plugin.test.domain.TestResultAspect.TestResultEvent;
import milkman.ui.plugin.PluginRequestExecutor;
import milkman.ui.plugin.Templater;
import milkman.utils.AsyncResponseControl.AsyncControl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.Optional;

import static milkman.plugin.test.domain.TestResultAspect.TestResultState.*;

@Slf4j
@RequiredArgsConstructor
public class TestRunner {

	private final PluginRequestExecutor executor;

	public ResponseContainer executeRequest(TestContainer request,
											Templater templater,
											AsyncControl asyncControl) {

		var testAspect = request.getAspect(TestAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Missing test aspect"));

		var testEnvironment = getOverrideEnvironment(testAspect);

		asyncControl.triggerReqeuestStarted();


		var replay = ReplayProcessor.<TestResultEvent>create();
		Flux<TestResultEvent> resultFlux = Flux.<TestResultEvent>create(sink -> {
			Flux.fromIterable(testAspect.getRequests())
					.index()
					.flatMap(tuple -> Mono.justOrEmpty(executor.getDetails(tuple.getT2().getId()).map(r -> Tuples.of(tuple.getT1(), tuple.getT2(), r))))
					.filter(t -> {
						var skip = t.getT2().isSkip();
						if (skip){
							sink.next(new TestResultEvent(t.getT1().toString(), t.getT3().getName(), SKIPPED, Map.of()));
						}
						return !skip;
					})
					.doOnNext(tuple -> sink.next(new TestResultEvent(tuple.getT1().toString(), tuple.getT3().getName(), STARTED, Map.of())))
					.flatMap(tuple -> execute(tuple, testEnvironment, sink))
					.onErrorContinue(err -> !testAspect.isStopOnFirstFailure(), (err, obj) -> {/* silently skip errors, they where signaled already */})
//				.switchIfEmpty(Mono.defer(() -> {
//					log.error("Request could not be found");
//					return Mono.just(new TestResultEvent("", "", TestResultAspect.TestResultState.EXCEPTION));
//				}))
					.doFinally(s -> {
						asyncControl.triggerRequestSucceeded();
						sink.complete();
					})
					.subscribeOn(Schedulers.elastic())
					.publish().connect();
		}).subscribeWith(replay);

		var container = new TestResultContainer();
		container.getAspects().add(new TestResultAspect(replay));
		container.getAspects().add(new TestResultEnvAspect(testEnvironment));
		return container;
	}


	private Environment getOverrideEnvironment(TestAspect testAspect){
		var environment = new Environment("override");
		environment.setActive(true);
		environment.setOrAdd("__TEST__", "true");
		testAspect.getEnvironmentOverride().forEach(entry -> environment.setOrAdd(entry.getName(), entry.getValue()));
		return environment;
	}

	private Mono<Void> execute(Tuple3<Long, TestDetails, RequestContainer> request, Environment overrideEnv, FluxSink<TestResultEvent> replay) {
		return Mono.defer(() -> Mono.just(executor.executeRequest(request.getT3(), Optional.of(overrideEnv))))
				.flatMap(res -> Mono.fromFuture(res.getStatusInformations()))
				.doOnNext(si -> replay.next(new TestResultEvent(request.getT1().toString(), request.getT3().getName(), SUCCEEDED, si)))
				.then()
				.onErrorResume(err -> request.getT2().isIgnore(), err -> {
					replay.next(new TestResultEvent(request.getT1().toString(), request.getT3().getName(), IGNORED, Map.of("exception", err.toString())));
					return Mono.empty();
				})
				.doOnError(t -> replay.next(new TestResultEvent(request.getT1().toString(), request.getT3().getName(), FAILED, Map.of("exception", t.toString()))));
	}
}
