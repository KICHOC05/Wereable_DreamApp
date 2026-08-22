import { existsSync, readdirSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const projectRoot = dirname(dirname(fileURLToPath(import.meta.url)));
const packageStore = join(projectRoot, "node_modules", ".pnpm");
const policyPath = join(projectRoot, "license-policy.json");

if (!existsSync(packageStore)) {
  console.error("node_modules/.pnpm is missing; run pnpm install first.");
  process.exit(1);
}

const policy = JSON.parse(readFileSync(policyPath, "utf8"));
const allowedLicenses = new Set(policy.allowedLicenses);
const packages = new Map();

function addPackage(packagePath) {
  const manifestPath = join(packagePath, "package.json");
  if (!existsSync(manifestPath)) return;

  const manifest = JSON.parse(readFileSync(manifestPath, "utf8"));
  if (!manifest.name || !manifest.version) return;

  const declaredLicense = typeof manifest.license === "string"
    ? manifest.license.trim()
    : Array.isArray(manifest.licenses)
      ? manifest.licenses.map((license) => license.type).filter(Boolean).join(" OR ")
      : "MISSING";

  packages.set(`${manifest.name}@${manifest.version}`, declaredLicense || "MISSING");
}

for (const storeEntry of readdirSync(packageStore, { withFileTypes: true })) {
  if (!storeEntry.isDirectory() || storeEntry.name === "node_modules") continue;

  const modulesPath = join(packageStore, storeEntry.name, "node_modules");
  if (!existsSync(modulesPath)) continue;

  for (const dependency of readdirSync(modulesPath, { withFileTypes: true })) {
    if (!dependency.isDirectory()) continue;
    const dependencyPath = join(modulesPath, dependency.name);

    if (dependency.name.startsWith("@")) {
      for (const scopedDependency of readdirSync(dependencyPath, { withFileTypes: true })) {
        if (scopedDependency.isDirectory()) {
          addPackage(join(dependencyPath, scopedDependency.name));
        }
      }
    } else {
      addPackage(dependencyPath);
    }
  }
}

const rejected = [...packages.entries()]
  .filter(([, license]) => !allowedLicenses.has(license))
  .sort(([left], [right]) => left.localeCompare(right));

console.log(`Reviewed ${packages.size} installed dependency versions.`);
if (rejected.length === 0) {
  console.log("All dependency licenses satisfy license-policy.json.");
  process.exit(0);
}

console.error("Dependencies with missing or unapproved licenses:");
for (const [packageName, license] of rejected) {
  console.error(`- ${packageName}: ${license}`);
}
process.exit(1);
