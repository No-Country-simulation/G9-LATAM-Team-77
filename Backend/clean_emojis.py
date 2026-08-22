import os
import glob
import emoji
import re

def is_emoji(char):
    return char in emoji.EMOJI_DATA

def remove_emojis_from_string(text):
    return emoji.replace_emoji(text, replace='')

def clean_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # We want to replace emojis in the whole file, but especially in comments
    # To be safe, just replacing emojis everywhere is fine for Java since emojis aren't part of Java syntax
    # and they probably used them in strings or comments.
    
    cleaned = remove_emojis_from_string(content)
    
    # Also clean up multiple spaces that might have been left behind by emoji removal
    # but only in comment blocks to avoid breaking formatting. 
    # Actually just replacing emoji is enough.
    
    if content != cleaned:
        print(f"Cleaned {filepath}")
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(cleaned)

def main():
    backend_dir = r"C:\Java\G9-LATAM-Team-77\Backend"
    java_files = glob.glob(os.path.join(backend_dir, "**", "*.java"), recursive=True)
    for f in java_files:
        clean_file(f)

if __name__ == "__main__":
    main()
