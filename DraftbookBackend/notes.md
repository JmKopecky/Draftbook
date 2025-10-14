# Dev Notes
## What we need
* Normal database for accounts
* Storage of text
* Normal database storage for the metadata around that text

## Data
* diff-patch-match for all our data stuff
* Store HTML documents on the server
* Pass those documents to the client on request
* Client creates a diff when editing, sending that diff to the server
    * When the server gets a diff, it updates the stored HTML doc to reflect that change.

## Work Components
* Title
* Subtitle
* Cover
* Summary
* Introductory Pages/frontmatter
    * Preface
    * Intro
    * Foreword
    * Dedication
    * Epigraph
    * Might be good to allow any of these or anything else the writer wants
* Designate the first chapter as a prologue (a chapter before the main chapters)

## Chapter Components
* Chapter title
* Chapter number
* Text contents

## Planning Components
* Per work:
    * Separable into folders (only one level deep, basically categories)
    * Folders consist of:
        * Name
        * Child notes
    * Each note consists of:
        * Name
        * Contents
* Per chapter:
    * Note contents for that chapter

## Account Components
* 