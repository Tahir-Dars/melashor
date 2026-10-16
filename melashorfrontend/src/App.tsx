import { Component, Suspense, type ReactNode } from "react";
import { ErrorState } from "./components/ErrorState";
import { LoadingScreen } from "./components/loadingScreen";
import { Route, Routes, useSearchParams } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { Navigate } from "react-router-dom";

class AppErrorBoundary extends Component<
  { children: ReactNode },
  { error: unknown }
> {
  override state = { error: null as unknown };

  static getDerivativeStateFromError(error: unknown) {
    return error;
  }

  override render() {
    if (this.state.error) {
      return <ErrorState error={this.state.error} />;
    }
    return this.props.children;
  }
}

function DashboardRoute() {
  const [searchParamas] = useSearchParams();
  const activeUserId = searchParamas.get("user") ?? "u1";

  return (
    <Suspense fallback={<LoadingScreen />}>
      <AppShell activeUserId={activeUserId} />
    </Suspense>
  );
}

function App() {
  return (
    <AppErrorBoundary>
      <Routes>
        <Route path="/" element={<DashboardRoute />} />
        <Route path="*" element={<Navigate to="/" replace/>}/>
      </Routes>
    </AppErrorBoundary>
  );
}

export default App;
