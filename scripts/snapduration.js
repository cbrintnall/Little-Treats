const fs = require('fs');
const path = require('path');

const SNAP_TO = 1200;
const INCREASE_FACTOR = 3;

function snapToNearest(value, snap) {
    return Math.round(value / snap) * snap;
}

const folder = process.argv[2] || '.';
const files = fs.readdirSync(folder).filter(f => f.endsWith('.json'));

for (const file of files) {
    const filePath = path.join(folder, file);
    const raw = fs.readFileSync(filePath, 'utf8');
    let data;

    try {
        data = JSON.parse(raw);
    } catch (e) {
        console.warn(`Skipping ${file} — invalid JSON: ${e.message}`);
        continue;
    }

    if (!data.modifications || !Array.isArray(data.modifications)) {
        console.warn(`Skipping ${file} — no modifications array`);
        continue;
    }

    const durations = data.modifications
        .map(m => m.duration)
        .filter(d => typeof d === 'number');

    if (durations.length === 0) {
        console.warn(`Skipping ${file} — no duration fields found`);
        continue;
    }

    const maxDuration = Math.max(...durations);
    const snapped = Math.max(snapToNearest(maxDuration, SNAP_TO), SNAP_TO);
    const finalDuration = snapped * INCREASE_FACTOR;

    data.modifications = data.modifications.map(m => ({
        ...m,
        duration: finalDuration
    }));

    fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
    console.log(`${file}: max=${maxDuration} → snapped=${snapped} → final=${finalDuration}`);
}

console.log(`\nDone. Processed ${files.length} file(s).`);