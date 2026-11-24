import { createFileRoute } from "@tanstack/react-router";
import { CalendarPlus, Check, Copy, Download } from "lucide-react";
import { useState } from "react";

import { Container, PageHero } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { Button } from "@/components/ui/button";
import { DISCORD_URL, ICS_URL } from "@/lib/elysium-data";

export const Route = createFileRoute("/calendrier")({
  head: () => ({
    meta: [
      { title: "Calendrier — Elysium Esport" },
      { name: "description", content: "Tournois, événements, streams et sessions ouvertes d'Elysium. Abonne-toi aux matchs officiels." },
      { property: "og:title", content: "Calendrier — Elysium Esport" },
      { property: "og:description", content: "Abonne-toi aux matchs officiels d'Elysium dans ton agenda." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: CalendarPage,
});

const STEPS = [
  "Copiez l'URL du flux ci-dessus.",
  "Google Agenda : « + » → « Depuis une URL » → collez le lien.",
  "Apple Calendar : Fichier → Nouvel abonnement au calendrier → collez le lien.",
  "Outlook : « Ajouter un calendrier » → « Depuis Internet » → collez le lien.",
];

function CalendarPage() {
  const [copied, setCopied] = useState(false);
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(ICS_URL);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  return (
    <>
      <PageHero eyebrow="Tournois · Streams · Événements" title="Calendrier" subtitle="Ne rate plus aucun combat d'Elysium." />

      <section className="relative pb-16">
        <Container className="grid gap-6 lg:grid-cols-[1.3fr_1fr]">
          <Reveal>
            <TiltCard max={4} className="panel clip-blade h-full p-7 sm:p-10">
              <CalendarPlus className="tilt-pop size-7 text-primary" />
              <h2 className="tilt-pop mt-5 font-display text-3xl font-bold uppercase">S'abonner aux matchs officiels</h2>
              <p className="mt-3 text-muted-foreground">
                Recevez automatiquement les matchs officiels (résultats et à venir) dans votre agenda : Google Agenda, Apple
                Calendar, Outlook…
              </p>
              <div className="mt-6 flex items-center gap-2 border border-border bg-background/60 p-2 pl-4 clip-blade-sm">
                <code className="min-w-0 flex-1 truncate font-label text-sm text-primary">{ICS_URL}</code>
                <Button variant="frame" size="sm" onClick={copy} aria-label="Copier le lien">
                  {copied ? <Check /> : <Copy />} {copied ? "Copié" : "Copier"}
                </Button>
              </div>
              <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                <Button asChild variant="gold" size="xl">
                  <a
                    href={`https://calendar.google.com/calendar/r?cid=${encodeURIComponent(ICS_URL.replace("https://", "webcal://"))}`}
                    target="_blank"
                    rel="noreferrer"
                  >
                    Ajouter à Google Agenda
                  </a>
                </Button>
                <Button asChild variant="frame" size="xl">
                  <a href={ICS_URL}>
                    <Download /> Télécharger (.ics)
                  </a>
                </Button>
              </div>
            </TiltCard>
          </Reveal>
          <Reveal delay={120}>
            <div className="panel clip-blade h-full p-7 sm:p-10">
              <p className="eyebrow mb-5">Comment s'abonner ?</p>
              <ol className="space-y-5">
                {STEPS.map((s, i) => (
                  <li key={s} className="flex gap-4">
                    <span className="font-display text-2xl font-black text-gold">{i + 1}</span>
                    <span className="pt-1 text-muted-foreground">{s}</span>
                  </li>
                ))}
              </ol>
            </div>
          </Reveal>
        </Container>

        <Container className="mt-6">
          <Reveal>
            <div className="panel clip-blade flex flex-col items-center gap-4 p-12 text-center">
              <p className="eyebrow">À venir</p>
              <p className="font-display text-2xl font-bold uppercase">Aucun événement à venir</p>
              <p className="max-w-md text-muted-foreground">Les prochains tournois et sessions ouvertes sont annoncés en premier sur le Discord.</p>
              <Button asChild variant="frame" size="xl">
                <a href={DISCORD_URL} target="_blank" rel="noreferrer">
                  Rejoindre le Discord
                </a>
              </Button>
            </div>
          </Reveal>
        </Container>
      </section>
    </>
  );
}
