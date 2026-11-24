import type { CSSProperties, ReactNode } from "react";

import { cn } from "@/lib/utils";

/** Title whose letters rise one by one in 3D. */
export function RiseTitle({ text, className }: { text: string; className?: string }) {
  return (
    <span aria-label={text}>
      {text.split("").map((ch, i) => (
        <span key={i} aria-hidden="true" className={cn("rise-letter", className)} style={{ "--i": i } as CSSProperties}>
          {ch === " " ? "\u00a0" : ch}
        </span>
      ))}
    </span>
  );
}

export function SectionHeading({
  eyebrow,
  title,
  action,
  className,
}: {
  eyebrow?: string;
  title: ReactNode;
  action?: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("mb-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between", className)}>
      <div>
        {eyebrow && <p className="eyebrow mb-3">{eyebrow}</p>}
        <h2 className="font-display text-3xl font-bold uppercase sm:text-4xl lg:text-5xl">{title}</h2>
      </div>
      {action}
    </div>
  );
}

export function PageHero({
  eyebrow,
  title,
  subtitle,
  children,
}: {
  eyebrow: string;
  title: string;
  subtitle?: string;
  children?: ReactNode;
}) {
  return (
    <section className="relative flex min-h-[78svh] items-end pb-16 pt-40 sm:min-h-[70vh] sm:items-center sm:pb-10">
      <div className="mx-auto w-full max-w-7xl px-5 sm:px-8">
        <p className="eyebrow mb-5">{eyebrow}</p>
        <h1 className="whitespace-nowrap font-display text-[clamp(2.1rem,9vw,6.5rem)] font-black uppercase leading-[0.95]">
          <RiseTitle text={title} className="text-gold" />
        </h1>
        {subtitle && (
          <p className="mt-6 max-w-xl text-lg text-muted-foreground sm:text-xl">{subtitle}</p>
        )}
        {children && <div className="mt-8">{children}</div>}
      </div>
    </section>
  );
}

export function Container({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn("mx-auto w-full max-w-7xl px-5 sm:px-8", className)}>{children}</div>;
}
