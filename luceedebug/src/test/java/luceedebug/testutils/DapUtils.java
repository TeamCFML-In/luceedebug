package luceedebug.testutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;

public class DapUtils {
    /**
     * This does not work recursively, and requires that exactly and only a single stop event (i.e. the target stop event)
     * be fired during the wait on the returned future.
     *
     * We might want to await this here, rather than allow the caller to do so, where if they forget to wait it's likely a bug.
     *
     * "do some work that should trigger the debugee to soon (microseconds) emit a stop event and return a future that resolves on receipt of that stopped event"
     */
    public static CompletableFuture<StoppedEventArguments> doWithStoppedEventFuture(MockClient client, Runnable f) {
        final var future = new CompletableFuture<StoppedEventArguments>();
        client.stopped_handler = stoppedEventArgs -> {
            client.stopped_handler = null; // concurrency issues? Callers should be synchronous with respect to this action though.
            future.complete(stoppedEventArgs);
        };
        f.run();
        return future;
    };

    public static CompletableFuture<SetBreakpointsResponse> setBreakpoints(
        IDebugProtocolServer dapServer,
        String filename,
        int ...lines
    ) {
        var source = new Source();
        source.setPath(filename);

        var breakpoints = new ArrayList<SourceBreakpoint>();
        for (var line : lines) {
            var bp = new SourceBreakpoint();
            bp.setLine(line);
            breakpoints.add(bp);
        }

        var breakpointsArgs = new SetBreakpointsArguments();
        
        breakpointsArgs.setSource(source);
        breakpointsArgs.setBreakpoints(
            breakpoints.toArray(new SourceBreakpoint[0])
        );
        
        return dapServer.setBreakpoints(breakpointsArgs);
    }

    public static CompletableFuture<Capabilities> init(IDebugProtocolServer dapServer) {
        var initArgs = new InitializeRequestArguments();
        initArgs.setClientID("test");
        return dapServer.initialize(initArgs);
    }
    
    public static CompletableFuture<Void> attach(IDebugProtocolServer dapServer) {
        return attach(dapServer, new HashMap<String,Object>());
    }

    public static CompletableFuture<Void> attach(IDebugProtocolServer dapServer, Map<String, Object> config) {
        return dapServer.attach(config);
    }

    public static CompletableFuture<StackTraceResponse> getStackTrace(IDebugProtocolServer dapServer, int threadID) {
        var stackTraceArgs = new StackTraceArguments();
        stackTraceArgs.setThreadId(threadID);
        return dapServer.stackTrace(stackTraceArgs);
    }

    public static CompletableFuture<ScopesResponse> getScopes(IDebugProtocolServer dapServer, int frameID) {
        var scopesArgs = new ScopesArguments();
        scopesArgs.setFrameId(frameID);
        return dapServer.scopes(scopesArgs);
    }

    public static CompletableFuture<VariablesResponse> getVariables(IDebugProtocolServer dapServer, Scope scope) {
        return getVariables(dapServer, scope.getVariablesReference());
    }

    public static CompletableFuture<VariablesResponse> getVariables(IDebugProtocolServer dapServer, int variableID) {
        var variablesArgs = new VariablesArguments();
        variablesArgs.setVariablesReference(variableID);
        return dapServer.variables(variablesArgs);
    }

    public static CompletableFuture<ContinueResponse> continue_(IDebugProtocolServer dapServer, int threadID) {
        var continueArgs = new ContinueArguments();
        continueArgs.setThreadId(threadID);
        return dapServer.continue_(continueArgs);
    }

    public static CompletableFuture<Void> disconnect(IDebugProtocolServer dapServer) {
        return dapServer.disconnect(new DisconnectArguments());
    }

    public static CompletableFuture<Void> stepIn(IDebugProtocolServer dapServer, int threadID) {
        var args = new StepInArguments();
        args.setThreadId(threadID);
        return dapServer.stepIn(args);
    }

    public static CompletableFuture<Void> stepOut(IDebugProtocolServer dapServer, int threadID) {
        var args = new StepOutArguments();
        args.setThreadId(threadID);
        return dapServer.stepOut(args);
    }

    public static CompletableFuture<Void> stepOver(IDebugProtocolServer dapServer, int threadID) {
        var args = new NextArguments();
        args.setThreadId(threadID);
        return dapServer.next(args);
    }

    public static CompletableFuture<EvaluateResponse> evaluate(IDebugProtocolServer dapServer, int frameID, String expr) {
        var args = new EvaluateArguments();
        args.setFrameId(frameID);
        args.setExpression(expr);
        return dapServer.evaluate(args);
    }

    public static class MockClient implements IDebugProtocolClient {
        public void breakpoint(BreakpointEventArguments args) {

        }
        public void continued(ContinuedEventArguments args) {

        }
        public void exited(ExitedEventArguments args) {

        }
        public void initialized() {
            
        }
        public void loadedSource(LoadedSourceEventArguments args) {

        }
        public void module(ModuleEventArguments args) {

        }
        public void output(OutputEventArguments args) {

        }
        public void process(ProcessEventArguments args) {

        }

        public Consumer<StoppedEventArguments> stopped_handler = null;
        public void stopped(StoppedEventArguments args) {
            if (stopped_handler != null) {
                stopped_handler.accept(args);
            }
        }

        public void terminated(TerminatedEventArguments args) {

        }
        public void thread(ThreadEventArguments args) {

        }
    }
}
