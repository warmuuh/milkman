package milkman.plugin.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.plugin.test.domain.TestContainer;
import milkman.plugin.test.domain.TestResultAspect;
import milkman.plugin.test.domain.TestResultContainer;
import milkman.ui.plugin.PluginRequestExecutor;
import milkman.ui.plugin.Templater;
import milkman.utils.AsyncResponseControl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
public class TestRunner {

	private final PluginRequestExecutor executor;

	public ResponseContainer executeRequest(TestContainer request,
											Templater templater,
											AsyncResponseControl.AsyncControl asyncControl) {

		var testAspect = request.getAspect(TestAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Missing test aspect"));

		asyncControl.triggerReqeuestStarted();


		var resultEvents = Flux.fromIterable(testAspect.getRequests())
				.flatMap(reqId -> {
					try {
						var response = executor.executeRequest(reqId);
						return response.map(rc -> Mono.fromFuture(rc.getStatusInformations()))
								.map(siMono -> siMono
										.map(si -> new TestResultAspect.TestResultEvent(reqId, reqId, TestResultAspect.TestResultState.SUCCEEDED))
										.onErrorResume(t -> {
											log.error("Failed to execute request", t);
											return Mono.just(new TestResultAspect.TestResultEvent(reqId, reqId, TestResultAspect.TestResultState.FAILED));
										})
								)
								.orElseGet(() -> {
									log.error("Request could not be found");
									return Mono.just(new TestResultAspect.TestResultEvent(reqId, reqId, TestResultAspect.TestResultState.EXCEPTION));
								});
					} catch (Exception e) {
						log.error("Something went wrong during request execution", e);
						return Mono.just(new TestResultAspect.TestResultEvent(reqId, reqId, TestResultAspect.TestResultState.EXCEPTION));
					}
				})
				.doOnComplete(asyncControl::triggerRequestSucceeded)
				.subscribeOn(Schedulers.parallel());

		var container = new TestResultContainer();
		container.getAspects().add(new TestResultAspect(resultEvents));
		return container;
	}
}
