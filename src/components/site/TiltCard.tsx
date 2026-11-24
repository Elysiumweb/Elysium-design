import { useRef, type HTMLAttributes, type PointerEvent } from "react";

import { cn } from "@/lib/utils";

type Props = HTMLAttributes<HTMLDivElement> & { max?: number };

/** Card that tilts in 3D toward the pointer, with a moving gold glare. */
export function TiltCard({ className, children, max = 10, ...rest }: Props) {
  const ref = useRef<HTMLDivElement>(null);

  const onMove = (e: PointerEvent<HTMLDivElement>) => {
    const el = ref.current;
    if (!el || e.pointerType === "touch") return;
    const r = el.getBoundingClientRect();
    const px = (e.clientX - r.left) / r.width;
    const py = (e.clientY - r.top) / r.height;
    el.style.setProperty("--ry", `${(px - 0.5) * max * 2}deg`);
    el.style.setProperty("--rx", `${(0.5 - py) * max * 2}deg`);
    el.style.setProperty("--mx", `${px * 100}%`);
    el.style.setProperty("--my", `${py * 100}%`);
    el.dataset["active"] = "true";
  };

  const onLeave = () => {
    const el = ref.current;
    if (!el) return;
    el.style.setProperty("--rx", "0deg");
    el.style.setProperty("--ry", "0deg");
    el.dataset["active"] = "false";
  };

  return (
    <div
      ref={ref}
      onPointerMove={onMove}
      onPointerLeave={onLeave}
      className={cn("tilt relative", className)}
      {...rest}
    >
      {children}
      <div className="tilt-glare" />
    </div>
  );
}
