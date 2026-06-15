# ATP match data

The CSVs are **not** checked into git (they're large and `.gitignore`d). Download
them here before running anything that needs real data.

## Where to get it

Jeff Sackmann's tennis_atp repository: https://github.com/JeffSackmann/tennis_atp

Drop the season files you want into **this folder**, e.g.:

```
atp_matches_2000.csv
atp_matches_2001.csv
...
atp_matches_2024.csv
matches_data_dictionary.txt   <- column meanings; you'll refer to it constantly
```

Quick fetch (a recent span — serve columns exist from ~1991, so 2000+ is safe):

```bash
cd src/main/resources/data
for y in $(seq 2000 2024); do
  curl -fsSLO "https://raw.githubusercontent.com/JeffSackmann/tennis_atp/master/atp_matches_${y}.csv"
done
curl -fsSLO "https://raw.githubusercontent.com/JeffSackmann/tennis_atp/master/matches_data_dictionary.txt"
```

## License / citation

Jeff Sackmann's tennis data is licensed **CC BY-NC-SA 4.0**
(https://creativecommons.org/licenses/by-nc-sa/4.0/). Non-commercial use only,
attribution required, share-alike. Cite the repository in your README and report.
