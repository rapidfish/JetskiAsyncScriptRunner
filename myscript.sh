#!/bin/bash

echo "--- Startar script ---"

# Fånga ALLA inkommande argument i en array direkt
SCRIPT_ARGS=("$@")

# Kontrollera om arrayen är tom
if [ ${#SCRIPT_ARGS[@]} -eq 0 ]; then
    echo "Inga parametrar mottogs."
    exit 1
fi

# Visa hur många element som hittades
echo "Antal argument i arrayen: ${#SCRIPT_ARGS[@]}"

# Loopa igenom arrayen och skriv ut
echo "Innehåll i arrayen:"
for i in "${!SCRIPT_ARGS[@]}"; do
    echo "  Index $i: ${SCRIPT_ARGS[$i]}"
done

echo "--- Script färdigt ---"