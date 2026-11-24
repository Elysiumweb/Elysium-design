// Contenu public d'Elysium, repris du site elysium-esport.fr.

export const BRAND = {
  icon: "https://elysium-esport.fr/brand/logo-icon-gold.png",
  horizontal: "https://elysium-esport.fr/brand/logo-horizontal-gold.png",
  vertical: "https://elysium-esport.fr/brand/logo-vertical-gold.png",
  blade: "https://elysium-esport.fr/brand/accent-blade.png",
  brackets: "https://elysium-esport.fr/brand/accent-brackets-gold.png",
  pattern: "https://elysium-esport.fr/brand/pattern.png",
} as const;

export const SOCIALS = [
  { label: "Discord", href: "https://discord.gg/RH3ZZkMJsw" },
  { label: "Twitch", href: "https://www.twitch.tv/elysiumxeva" },
  { label: "YouTube", href: "https://www.youtube.com/@elysiumfr" },
  { label: "X", href: "https://x.com/ElysiumxEVA" },
  { label: "Instagram", href: "https://www.instagram.com/elysiumxeva/" },
  { label: "TikTok", href: "https://www.tiktok.com/@elysiumxeva" },
] as const;

export const DISCORD_URL = "https://discord.gg/RH3ZZkMJsw";
export const TWITCH_URL = "https://www.twitch.tv/elysiumxeva";
export const YOUTUBE_URL = "https://www.youtube.com/@elysiumfr";
export const ICS_URL = "https://elysium-esport.fr/matches.ics";

export type Game = "EVA" | "Rocket League";

export type Player = {
  name: string;
  game: Game;
  role: string;
  bio?: string;
  photo: string;
};

export const PLAYERS: Player[] = [
  {
    name: "Aalltrac",
    game: "EVA",
    role: "Rusher",
    bio: "Joueur français, 18 ans. Joueur EVA et Game Master EVA Savigny-Le-Temple.",
    photo: "https://i.ibb.co/Nzb74Fh/Logo.png",
  },
  {
    name: "Clemkuss",
    game: "EVA",
    role: "Teneur de ligne",
    photo: "https://i.ibb.co/G4yFtR9Y/Gemini-Generated-Image-dbe7ljdbe7ljdbe7.png",
  },
  {
    name: "Piougelo",
    game: "EVA",
    role: "Rusher",
    bio: "Angelo alias Piougelo. Pusher et dueliste au mental de performeur : je ne me laisse pas trahir par les émotions, déterminé à m’améliorer.",
    photo: "https://i.ibb.co/sZ9dzpJ/BE18291-E-F1-F8-4-F84-82-CD-EB71-B3050-B57.png",
  },
  {
    name: "Wayzer",
    game: "EVA",
    role: "Lineur / Meneur",
    bio: "Joueur EVA engagé, mental solide et déterminé à performer au plus haut niveau avec son équipe.",
    photo: "https://i.ibb.co/vvrKp1GR/EF3-EDA7-A-CC9-A-4113-B49-C-69603-C977918.png",
  },
];

export type MatchResult = {
  team: string;
  opponent: string;
  game: Game;
  us: number;
  them: number;
  date: string; // ISO, heure de Paris
  competition: string;
  mvp?: string;
};

export const RESULTS: MatchResult[] = [
  { team: "Elysium Ascend", opponent: "LIONERA ESPORT", game: "Rocket League", us: 5, them: 1, date: "2026-11-09T20:55", competition: "BO7", mvp: "Ascend 6" },
  { team: "Elysium Mechanical", opponent: "Valkor", game: "Rocket League", us: 1, them: 0, date: "2026-11-07T20:55", competition: "BO7", mvp: "Mechanical 1" },
  { team: "Elysium Ascend", opponent: "DARKGAMES HORUS", game: "Rocket League", us: 2, them: 3, date: "2026-11-07T14:00", competition: "Spring Cup #4" },
  { team: "Elysium Ascend", opponent: "TTS | UMBRIX", game: "Rocket League", us: 3, them: 0, date: "2026-11-07T14:00", competition: "Spring Cup #4" },
  { team: "Elysium Ascend", opponent: "REDBLOOD ESPORT", game: "Rocket League", us: 3, them: 0, date: "2026-11-07T14:00", competition: "Spring Cup #4" },
  { team: "Elysium Ascend", opponent: "EXALTIA EGO", game: "Rocket League", us: 3, them: 1, date: "2026-11-07T14:00", competition: "Spring Cup #4" },
  { team: "Elysium Ascend", opponent: "SNIR ACADÉMIE", game: "Rocket League", us: 2, them: 3, date: "2026-11-07T14:00", competition: "Spring Cup #4" },
  { team: "Elysium Ascend", opponent: "Vyse eSports", game: "Rocket League", us: 4, them: 2, date: "2026-11-06T20:55", competition: "BO7", mvp: "Ascend 6" },
  { team: "Elysium Ascend", opponent: "Electium eS", game: "Rocket League", us: 4, them: 2, date: "2026-11-05T20:55", competition: "BO7", mvp: "Ascend 6" },
];

/** Bilan officiel affiché par le club (toutes saisons). */
export const SEASON_STATS = { matches: 46, winRate: 74 } as const;

export const DONATION_USES = [
  { title: "Matériel & serveurs", text: "Périphériques, licences, serveurs d'entraînement et outils de production pour les streams." },
  { title: "Déplacements en LAN", text: "Transport, hébergement et repas des joueurs lors des tournois en présentiel." },
  { title: "Inscriptions & compétitions", text: "Frais d'inscription aux ligues et tournois officiels sur EVA et Rocket League." },
  { title: "Vie de la communauté", text: "Événements Discord, lots pour les tournois communautaires et créations visuelles." },
] as const;

export const RECRUITMENT_STEPS = [
  { title: "Reçu", text: "Votre dossier est enregistré et les manageurs sont notifiés.", days: 2 },
  { title: "Test", text: "Analyse du profil, des VOD et des disponibilités.", days: 5 },
  { title: "Essai / Entretien", text: "Scrim, essai en jeu ou appel Discord avec un manageur.", days: 4 },
  { title: "Décision", text: "Réponse définitive : intégration au roster ou refus.", days: 2 },
] as const;

export const PRODUCTS = {
  jersey: {
    name: "Maillot officiel Elysium 2026",
    price: "49,90 €",
    image: "https://i.ibb.co/ZR8f8kCd/13.jpg",
    href: "https://eliminate.fr/elysium",
    text: "Un maillot sublimé léger et respirant, conçu pour la compétition comme pour soutenir Elysium au quotidien.",
    features: ["Maille respirante 100 % polyester européen", "Fabriqué et assemblé en France"],
    sizes: ["XS", "S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL"],
  },
  sleeves: {
    name: "Manchettes Elysium 2026",
    image: "https://i.ibb.co/tShCYSk/12.jpg",
    href: "https://eliminate.fr/produit/elysium-manchette-2026/",
    text: "Les couleurs du maillot au bout des bras, à porter sous le maillot ou seules.",
    prices: [
      { label: "L'unité", price: "19,90 €" },
      { label: "La paire", price: "29,90 €" },
    ],
  },
} as const;

export function isWin(r: MatchResult) {
  return r.us > r.them;
}

export function formatMatchDate(iso: string) {
  const [d, t] = iso.split("T");
  const [y, m, day] = (d ?? "").split("-").map(Number);
  const months = ["janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"];
  const days = ["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"];
  const date = new Date(Date.UTC(y ?? 2026, (m ?? 1) - 1, day ?? 1));
  return `${days[date.getUTCDay()]} ${day} ${months[(m ?? 1) - 1]} · ${t ?? ""}`;
}
