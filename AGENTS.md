<!-- LOVABLE:BEGIN -->
> [!IMPORTANT]
> This project is connected to [Lovable](https://lovable.dev). Avoid rewriting
> published git history — force pushing, or rebasing/amending/squashing commits
> that are already pushed — as it rewrites history on Lovable's side and the
> user will likely lose their project history.
>
> Commits you push to the connected branch sync back to Lovable and show up in
> the editor, so keep the branch in a working state.

## What Lovable manages

Rules outside this block are the project's own. Change the pieces below in
Lovable, not by hand. With the [Lovable MCP](https://mcp.lovable.dev)
connected, `send_message` asks Lovable to do it; otherwise ask the user to
use the editor.

- **Database.** Do not add or edit `supabase/migrations/` or `drizzle/`.
  Ask Lovable to change the schema.
- **Generated files.** Do not edit a file whose header says it is automatically
  generated.
- **AI gateway.** Keep model calls on Lovable's gateway, and keep
  `LOVABLE_API_KEY` out of browser code.
- **Connectors.** Lovable provides native connectors. `list_connectors` shows
  which are enabled; `add_connector` only returns the dashboard link. When a
  connector is enabled, calls go through the connector gateway and need
  `LOVABLE_API_KEY`. When Lovable does not provide one, users can bring their
  own key and store it in Secrets.
- **Secrets.** Lovable manages them. Do not put secret values in the repo.
<!-- LOVABLE:END -->

## Project rules

- Public site content (players, results, products, links) lives in `src/lib/elysium-data.ts`; pages read from it so content edits stay in one place. No backend yet.
- One fixed 3D backdrop (`src/components/site/SceneBackground.tsx` → lazy `src/three/EmblemCanvas.tsx`) is mounted once in `__root`; each page only declares the emblem pose in `src/three/scene-targets.ts`, so the emblem glides between pages instead of remounting.
- The 3D emblem geometry comes from `src/three/emblem-path.ts`, a potrace trace of the official gold logo; re-trace it if the logo changes.
- The canvas is client-only (lazy import after a WebGL check) so SSR never touches three.js.
