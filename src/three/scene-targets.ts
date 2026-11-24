// Where the 3D emblem sits on each page. Pure data so pages stay easy to tune.
// x / y are in scene units (camera sees ~10 x 5.7 units on desktop), scale 1 = full emblem,
// explode 0 = assembled, 1+ = shards flying apart.

export type SceneTarget = {
  x: number;
  y: number;
  scale: number;
  explode: number;
  tilt: number;
};

export type SceneTargetSet = { desktop: SceneTarget; mobile: SceneTarget };

const T = (x: number, y: number, scale: number, explode: number, tilt = 0): SceneTarget => ({
  x,
  y,
  scale,
  explode,
  tilt,
});

export const SCENE_TARGETS: Record<string, SceneTargetSet> = {
  "/": { desktop: T(2.35, 0.05, 1, 0), mobile: T(0, 1.2, 0.5, 0) },
  "/equipe": { desktop: T(3, 0.4, 0.82, 0.35, 0.3), mobile: T(0.3, 1.25, 0.45, 0.4) },
  "/resultats": { desktop: T(3.1, 0.35, 0.78, 0.55, -0.4), mobile: T(-0.2, 1.2, 0.45, 0.6) },
  "/calendrier": { desktop: T(2.9, -0.2, 0.75, 0.85, 0.5), mobile: T(0.2, 1.2, 0.45, 0.8) },
  "/recrutement": { desktop: T(2.6, 0.1, 0.95, 1.35, -0.2), mobile: T(0, 1.1, 0.5, 1.2) },
  "/boutique": { desktop: T(3.2, 0.5, 0.75, 0.2, 0.6), mobile: T(0, 1.25, 0.45, 0.25) },
  "/soutenir": { desktop: T(2.9, 0.3, 0.85, 0.12, -0.5), mobile: T(0, 1.2, 0.48, 0.15) },
};

export function sceneTargetFor(pathname: string, portrait: boolean): SceneTarget {
  const key = pathname.length > 1 ? pathname.replace(/\/+$/, "") : "/";
  const set = SCENE_TARGETS[key] ?? SCENE_TARGETS["/equipe"]!;
  return portrait ? set.mobile : set.desktop;
}
