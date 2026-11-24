import { Link } from "@tanstack/react-router";
import { Heart, Menu } from "lucide-react";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { BRAND } from "@/lib/elysium-data";
import { cn } from "@/lib/utils";

export const NAV = [
  { to: "/", label: "Accueil" },
  { to: "/equipe", label: "L'équipe" },
  { to: "/resultats", label: "Résultats" },
  { to: "/calendrier", label: "Calendrier" },
  { to: "/recrutement", label: "Recrutement" },
  { to: "/boutique", label: "Boutique" },
] as const;

export function SiteHeader() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={cn(
        "fixed inset-x-0 top-0 z-50 transition-[background-color,border-color,backdrop-filter] duration-500",
        scrolled ? "border-b border-border bg-background/70 backdrop-blur-xl" : "border-b border-transparent",
      )}
    >
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-5 sm:h-20 sm:px-8">
        <Link to="/" className="shrink-0" aria-label="Elysium — accueil">
          <img src={BRAND.horizontal} alt="Elysium" className="h-7 w-auto sm:h-8" width={963} height={304} />
        </Link>

        <nav className="hidden items-center gap-7 lg:flex" aria-label="Navigation principale">
          {NAV.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              activeOptions={{ exact: item.to === "/" }}
              className="group relative font-label text-xs font-semibold uppercase tracking-[0.24em] text-muted-foreground transition-colors hover:text-foreground data-[status=active]:text-primary"
            >
              {item.label}
              <span className="absolute -bottom-2 left-0 h-px w-0 bg-primary transition-all duration-300 group-hover:w-full group-data-[status=active]:w-full" />
            </Link>
          ))}
        </nav>

        <div className="hidden items-center gap-3 lg:flex">
          <Button asChild variant="frame" size="sm" className="h-10 px-4">
            <Link to="/soutenir">
              <Heart /> Soutenir
            </Link>
          </Button>
          <Button asChild variant="gold" size="sm" className="h-10 px-5">
            <Link to="/recrutement">Rejoindre</Link>
          </Button>
        </div>

        <Sheet open={open} onOpenChange={setOpen}>
          <SheetTrigger asChild>
            <Button variant="frame" size="icon" className="h-11 w-11 lg:hidden" aria-label="Ouvrir le menu">
              <Menu className="size-5!" />
            </Button>
          </SheetTrigger>
          <SheetContent side="right" className="w-full border-border bg-background/95 backdrop-blur-xl sm:max-w-sm">
            <SheetTitle className="sr-only">Menu</SheetTitle>
            <nav className="mt-14 flex flex-col gap-1 px-2" aria-label="Navigation mobile">
              {[...NAV, { to: "/soutenir", label: "Soutenir" } as const].map((item, i) => (
                <Link
                  key={item.to}
                  to={item.to}
                  onClick={() => setOpen(false)}
                  activeOptions={{ exact: item.to === "/" }}
                  className="flex items-baseline gap-4 border-b border-border py-4 font-display text-2xl font-bold uppercase text-foreground transition-colors data-[status=active]:text-primary"
                >
                  <span className="font-label text-xs text-primary">0{i + 1}</span>
                  {item.label}
                </Link>
              ))}
            </nav>
            <div className="mt-8 px-2">
              <Button asChild variant="gold" size="xl" className="w-full">
                <Link to="/recrutement" onClick={() => setOpen(false)}>
                  Nous rejoindre
                </Link>
              </Button>
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </header>
  );
}
