import { useRouterState } from "@tanstack/react-router";
import { Component, Suspense, lazy, useEffect, useState, type ReactNode } from "react";

const EmblemCanvas = lazy(() => import("@/three/EmblemCanvas"));

class SceneBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  override state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  override render() {
    return this.state.failed ? null : this.props.children;
  }
}

function hasWebGL() {
  try {
    const c = document.createElement("canvas");
    return !!(c.getContext("webgl2") || c.getContext("webgl"));
  } catch {
    return false;
  }
}

/** Fixed 3D backdrop shared by every public page; the emblem glides to a new pose per page. */
export function SceneBackground() {
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    setEnabled(hasWebGL());
  }, []);

  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 z-0 bg-background">
      {enabled && (
        <SceneBoundary>
          <Suspense fallback={null}>
            <EmblemCanvas pathname={pathname} />
          </Suspense>
        </SceneBoundary>
      )}
      <div className="veil absolute inset-0" />
    </div>
  );
}
