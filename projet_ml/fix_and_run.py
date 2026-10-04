# -*- coding: utf-8 -*-
"""
Script de correction et d'execution du notebook ML Adult.
"""

import sys
import io
# Forcer UTF-8 pour la sortie console
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

import json
import subprocess
import os
import uuid

NOTEBOOK_PATH = "ml_adult_pipeline.ipynb"

print("[FIX] Correction du notebook en cours...")

# -- 1. Charger le JSON du notebook
with open(NOTEBOOK_PATH, "r", encoding="utf-8") as f:
    nb = json.load(f)

# -- 2. Ajouter des IDs manquants aux cellules (evite le warning nbformat)
for cell in nb["cells"]:
    if "id" not in cell:
        cell["id"] = str(uuid.uuid4())[:8]

# -- 3. Corriger la f-string invalide dans les cellules de code
fixed_count = 0
for cell in nb["cells"]:
    if cell["cell_type"] == "code":
        src = cell["source"]
        if isinstance(src, list):
            new_src = []
            for line in src:
                if "auc:.4f if auc else" in line:
                    # Remplacer par 2 nouvelles lignes correctes
                    new_src.append(
                        "    auc_str = f'{auc:.4f}' if auc is not None else 'N/A'\n"
                    )
                    new_src.append(
                        "    print(f'OK {name:<30} | Acc: {acc:.4f} | F1: {f1:.4f} | AUC: {auc_str} | CV: {cv_scores.mean():.4f}+-{cv_scores.std():.4f}')\n"
                    )
                    fixed_count += 1
                else:
                    new_src.append(line)
            cell["source"] = new_src
        elif isinstance(src, str) and "auc:.4f if auc else" in src:
            cell["source"] = src.replace(
                '{auc:.4f if auc else "N/A"}',
                '{auc_str}'
            )
            fixed_count += 1

print(f"   [OK] {fixed_count} bug(s) corrige(s)")

# -- 4. Vider les outputs precedents pour une execution propre
for cell in nb["cells"]:
    if cell["cell_type"] == "code":
        cell["outputs"] = []
        cell["execution_count"] = None

# -- 5. Sauvegarder le notebook corrige
with open(NOTEBOOK_PATH, "w", encoding="utf-8") as f:
    json.dump(nb, f, ensure_ascii=False, indent=1)

print("   [OK] Notebook sauvegarde avec corrections")
print()
print("[RUN] Lancement de l'execution complete du notebook...")
print("   (Peut prendre 3-8 minutes pour 49 000 lignes et 6 modeles ML)")
print()

# -- 6. Executer le notebook avec jupyter nbconvert
jupyter_path = r"C:\Users\rachi\anaconda3\Scripts\jupyter.exe"

cmd = [
    jupyter_path,
    "nbconvert",
    "--to", "notebook",
    "--execute",
    "--inplace",
    "--ExecutePreprocessor.timeout=600",
    "--ExecutePreprocessor.kernel_name=python3",
    NOTEBOOK_PATH
]

result = subprocess.run(cmd, capture_output=True, text=True,
                        encoding="utf-8", errors="replace")

if result.stdout:
    print("STDOUT:", result.stdout[:2000])
if result.stderr:
    print("STDERR:", result.stderr[:2000])

if result.returncode == 0:
    print()
    print("=" * 60)
    print("[SUCCES] Le notebook a ete execute avec succes !")
    print(f"   Fichier : {os.path.abspath(NOTEBOOK_PATH)}")
    print()
    images = sorted([f for f in os.listdir(".") if f.endswith(".png")])
    if images:
        print(f"   {len(images)} graphiques PNG generes :")
        for img in images:
            print(f"      - {img}")
    print("=" * 60)
else:
    print()
    print("[ERREUR] Code de retour:", result.returncode)
    sys.exit(1)
