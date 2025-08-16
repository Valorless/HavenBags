package valorless.havenbags.utils;

import java.util.*;
import org.bukkit.event.Listener;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;

public class NoteBlockUtils implements Listener {

	// Note IDs are 0–11 for F# to E (12 notes in an octave)
	// NoteBlock.getNote().getId() returns 0–24 (across octaves)

	@SuppressWarnings("deprecation")
	public static Boolean compateNoteBlock(NoteBlock noteBlock, String instrument, int note) {
		if (noteBlock == null || instrument == null) {
			Log.Debug(Main.plugin, "NoteBlock or instrument is null.");
			return false;
		}

		// Check if the NoteBlock's instrument matches
		Log.Debug(Main.plugin, "Comparing NoteBlock instrument: " + noteBlock.getInstrument().toString() + " with instrument: " + instrument);
		if (!noteBlock.getInstrument().toString().equalsIgnoreCase(instrument)) {
			return false;
		}

		// Check if the NoteBlock's note matches
		try {
			Log.Debug(Main.plugin, "Comparing NoteBlock note: " + noteBlock.getNote().getId() + " with note: " + note);
			return noteBlock.getNote().getId() == note;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false; // Invalid note name
		}
	}
	
	// Defines the 12 semitones in one octave (F#0=0)
    private static final Tone[] TONES = {
        Tone.F, Tone.F, Tone.G, Tone.G, Tone.A, Tone.A, Tone.B, Tone.C, Tone.C, Tone.D, Tone.D, Tone.E
    };
    private static final boolean[] SHARPS = {
        true, false, true, false, true, false, false, true, false, true, false, false
    };

    public static final HashMap<Integer, Note> noteIndexMap = new HashMap<>();

    static {
        for (int i = 0; i <= 24; i++) {
            int octave = i / 12;          // 0, 1, or 2
            int noteIndex = i % 12;       // 0 - 11
            Tone tone = TONES[noteIndex];
            boolean sharp = SHARPS[noteIndex];
            noteIndexMap.put(i, new Note(octave, tone, sharp));
        }
    }
    
    public static Note getNote(int index) {
    			if (index < 0 || index > 24) {
			throw new IllegalArgumentException("Note index must be between 0 and 24.");
		}
		return noteIndexMap.get(index);
    }
    
}
