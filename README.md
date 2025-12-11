# Draftbook
A tool for writers to bring their ideas into reality.

Built as a portfolio project by Joseph Kopecky. 

## Implemented Features
* Auth0 Authentication
* Dashboard displaying works
  * Config:
    * Title
    * Subtitle
  * Delete
  * Create
  * Edit
* Work editor interface
  * Content editor main panel
  * Menu for chapters:
    * Config:
      * Title
    * Create
    * Delete
    * Rename
    * Select for editing
  * Menu for notes:
    * Separated by note categories
      * Default one for chapters, where each chapter has notes specifically for it.
      * Create
      * Rename
      * Delete
      * Notes:
        * Rename
        * Delete
        * Create
        * Access note editor
    * Separate mode for editing notes
* Save content by sending the entire HTML file to the server and back each time.
  * Diff-checking to make handling large content files easier.
* Focus mode for chapters and notes, allowing you to full-screen notes as well

## Planned Features
* Account page (assess requirements)
* Home/intro page