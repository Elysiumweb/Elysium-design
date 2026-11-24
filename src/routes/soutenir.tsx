import { createFileRoute } from "@tanstack/react-router";
import { ArrowUpRight, HeartHandshake, MonitorSmartphone, Plane, Trophy, Users } from "lucide-react";

import { Container, PageHero, SectionHeading } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { Button } from "@/components/ui/button";
import { DISCORD_URL, DONATION_USES } from "@/lib/elysium-data";

export const Route = createFileRoute("/soutenir")({
  head: () => ({
    meta: [
      { title: "Soutenir Elysium — Elysium Esport" },
      { name: "description", content: "Chaque don finance le matériel, les déplacements en LAN et les inscriptions des rosters Elysium." },
      { property: "og:title", content: "Soutenir Elysium" },
      { property: "og:description", content: "Rien n'est donné. Tout se mérite — avec vous. Soutenez l'association Elysium." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: SupportPage,
});

const ICONS = [MonitorSmartphone, Plane, Trophy, Users];

const TRANSPARENCY = [
  "100 % des dons sont affectés au fonctionnement sportif et associatif d'Elysium. Aucun reversement personnel.",
  "Les comptes de l'association sont présentés chaque année à l'assemblée générale de ses membres.",
  "Une question sur l'utilisation des fonds ? Écrivez-nous : nous répondons à toutes les demandes.",
];

function SupportPage() {
  return (
    <>
      <PageHero
        eyebrow="Soutien à l'association"
        title="Soutenir"
        subtitle="Chaque don finance directement la structure. Rien n'est donné. Tout se mérite — avec vous."
      >
        <Button asChild variant="gold" size="xl">
          <a href={DISCORD_URL} target="_blank" rel="noreferrer">
            <HeartHandshake /> Faire un don
          </a>
        </Button>
      </PageHero>

      <section className="relative py-16">
        <Container className="grid gap-10 lg:grid-cols-2">
          <Reveal>
            <p className="eyebrow mb-4">Pourquoi nous soutenir</p>
            <p className="text-lg leading-relaxed text-muted-foreground sm:text-xl">
              Elysium est une association loi 1901 portée par des bénévoles et des joueurs. Nous ne vivons ni des transferts
              ni des droits TV : nos moyens viennent de nos partenaires et de celles et ceux qui croient au projet.
            </p>
          </Reveal>
          <Reveal delay={120}>
            <p className="font-display text-2xl font-bold uppercase leading-snug sm:text-3xl">
              Votre don permet à nos rosters de se concentrer sur ce qui compte : <span className="text-gold">la performance.</span>
            </p>
            <p className="mt-4 text-muted-foreground">Chaque euro reste dans la structure.</p>
          </Reveal>
        </Container>
      </section>

      <section className="relative py-16">
        <Container>
          <Reveal>
            <SectionHeading eyebrow="Où va votre don" title={<>Chaque euro <span className="text-gold">compte</span></>} />
          </Reveal>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {DONATION_USES.map((u, i) => {
              const Icon = ICONS[i] ?? Trophy;
              return (
                <Reveal key={u.title} delay={i * 100}>
                  <TiltCard max={12} className="panel clip-blade h-full p-7">
                    <Icon className="tilt-pop-lg size-8 text-primary" />
                    <h3 className="tilt-pop mt-6 font-display text-lg font-bold uppercase">{u.title}</h3>
                    <p className="mt-2 text-muted-foreground">{u.text}</p>
                  </TiltCard>
                </Reveal>
              );
            })}
          </div>
        </Container>
      </section>

      <section className="relative py-16">
        <Container>
          <Reveal>
            <SectionHeading eyebrow="Engagements" title="Transparence" />
          </Reveal>
          <div className="grid gap-5 md:grid-cols-3">
            {TRANSPARENCY.map((t, i) => (
              <Reveal key={t} delay={i * 100}>
                <div className="panel clip-blade h-full p-7">
                  <p className="font-display text-5xl font-black text-outline">0{i + 1}</p>
                  <p className="mt-4 text-muted-foreground">{t}</p>
                </div>
              </Reveal>
            ))}
          </div>
          <p className="mt-8 max-w-3xl text-sm text-muted-foreground">
            Elysium est une association régie par la loi du 1er juillet 1901 (RNA : W772011943). Les dons versés ne
            constituent pas un achat et n'ouvrent droit à aucune contrepartie ni, en l'état, à une réduction d'impôt. Ils ne
            sont pas remboursables, sauf erreur manifeste : contactez-nous dans ce cas.
          </p>
        </Container>
      </section>

      <section className="relative py-16">
        <Container>
          <Reveal>
            <SectionHeading eyebrow="Autres façons de nous aider" title={<>Rejoindre <span className="text-gold">l'aventure</span></>} />
          </Reveal>
          <div className="grid gap-5 md:grid-cols-2">
            {[
              {
                title: "Devenir partenaire",
                text: "Vous représentez une entreprise ? Associez votre image à une structure ambitieuse et visible.",
              },
              {
                title: "Rejoindre la communauté",
                text: "Suivez nos matchs, participez aux événements Discord et faites vivre l'écusson.",
              },
            ].map((c, i) => (
              <Reveal key={c.title} delay={i * 120}>
                <a href={DISCORD_URL} target="_blank" rel="noreferrer" className="group block h-full">
                  <TiltCard max={6} className="panel clip-blade h-full p-8">
                    <h3 className="tilt-pop font-display text-2xl font-bold uppercase">{c.title}</h3>
                    <p className="mt-3 text-muted-foreground">{c.text}</p>
                    <span className="mt-6 inline-flex items-center gap-2 font-label text-xs font-semibold uppercase tracking-[0.22em] text-primary">
                      Nous contacter <ArrowUpRight className="size-4 transition-transform group-hover:-translate-y-1 group-hover:translate-x-1" />
                    </span>
                  </TiltCard>
                </a>
              </Reveal>
            ))}
          </div>
        </Container>
      </section>
    </>
  );
}
