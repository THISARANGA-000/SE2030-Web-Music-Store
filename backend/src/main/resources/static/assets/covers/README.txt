MelodyMart Album Cover Image Directory
======================================

Place album cover images in this folder using the filenames referenced by dbo.ALBUM.CoverImageURL.

When an image file with a matching filename is placed here, Spring Boot serves it statically at:
/assets/covers/<filename>

Referenced album cover filenames in dbo.ALBUM:
- sanda_eliya.jpg           (Album ID 1: Sanda Eliya)
- hithata_ahimi.jpg         (Album ID 2: Hithata Ahimi)
- colombo_after_rain.jpg    (Album ID 3: Colombo After Rain)
- kandyan_skies.jpg         (Album ID 5: Kandyan Skies)
- island_echoes.jpg         (Album ID 6: Island Echoes)
- ocean_road.jpg            (Album ID 7: Ocean Road)
- tropical_memories.jpg     (Album ID 8: Tropical Memories)
- baila_night.jpg           (Album ID 9: Baila Night in Colombo)
- city_lights.jpg           (Album ID 10: City Lights)
- southern_breeze.jpg       (Album ID 11: Southern Breeze)
- yarl_isai.jpg             (Album ID 12: Yarl Isai)

External image URLs (e.g., https://images.unsplash.com/...) can also be stored in CoverImageURL and will be rendered directly by the templates.
