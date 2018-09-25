# Release and Versioning

Babel has been deployed to clojars, and has been set up to make use of lein-v to
streamline releases. This file contains instructions on how to update, version and otherwise publish babel.
This information is derived from [here](https://github.com/roomkey/lein-v#support-for-lein-release).
I assume you have a gpg key set up, and are part of the ``babel-middleware`` clojars group.

## Instructions

### Checking Compatibility
First, you may test if your new code breaks anything. If you run ``lein install``, it should make your present branch appear as a package on your computer. It will display a specific version hash, that you can use to make sure you are requiring the new code.

If installing the new code works in a blank project, you are ready to proceed.

### Marking A Release
Assuming your new code is in a branch, make a final commit on that branch, then check out ``master``. Pull from upstream, and then merge in your branch, and commit the result.

If you make any changes to files after the merge, you will have to recommit them. Once you have the code ready, run ``lein v release :keyword`` where keyword is one of the following. ``:major`` ,``:minor`` or ``:patch``. Other keyword options exist, such as adding ``-alpha`` to the keyword. The options are documented [here](https://github.com/roomkey/lein-v/blob/master/README.md#support-for-lein-release), and semantic versioning is documented [here](https://semver.org/).

Push to ``master``. I do not know how well this works in a pull request scenario.

### Deploying to Clojars
After you have run the above, the code is marked and versioned in git, and is on github.
To publish it to clojars, run ``lein deploy clojars`` with a clean working directory, on the commit that the previous step produced. Provide identification, and wait for the upload to complete. Verify the new version is up on the site, and optionally try replacing the test dependency in your test project with it.

 
