# CSE360Wed34
## This is our repository for our team projects for CSE360, Wednesday section, group 34, Spring 2025 semester.
### Team members include: Eliza Chook, Anas Mohamed Riyaz, Nathaniel Teo, Isabella Tunon-Robinson, Tharun Vimalaadhithan, Zakia Mohammadi

### Functions added Related to Reviews and Reviewers (Phase 3 Work)
- Reviewer's ability to create reviews of potential answers so the rest of the class can benefit from their experiences. 
- Reviewer's ability to update a review based on inputs received and new insights gained, and others can see their new review that contains a link to the previous review so others can see both their original and updated review and appreciate the change.
- Student's ability to request to be given the role of a reviewer so their classmates can benefit from their experience and reduce the number of potential answers they need to read. An instructor must approve this request, and the role is assigned upon approval.
- Student's ability to establish and manage a list of reviewers they trust and assign each a weight, so their curated list of answers serves me well.
- Student's ability to read the reviews of potential answers to questions and add promising reviewers to their potential trusted reviewers list so they can establish and manage a list of trusted reviewers.
- Student's ability to see when a reviewer in their list of trusted reviewers updates a review, so they can quickly benefit from the update.
- Reviewer's ability to establish and maintain a Reviewer Profile, which includes details about their experience, a list of all the reviews they have provided, and feedback they have received from students who have used their reviews.
- Student's ability to provide private feedback to a reviewer of a potential answer so the reviewer can provide better reviews.
- Reviewer's ability to access a list of their reviews and see the number of private feedback messages for each so that they can manage and improve my reviews.


### Functions added for Student Role (Phase 2 Work)
- can ask questions and receive a list of potential answers
- can produce a new question based on a previous question to address the feedback I have received and any new insights gained, so I am more likely to get an answer that resolves my issue.
- can ask questions and receive a curated list of potential answers from sources I trust (users) so I can quickly get back to work.
- can specify that a specific potential answer resolves my issue so others can benefit from my experience. The system provides a separate access method so others can quickly see the answers that resolve issues without needing to traverse the earlier potential answers that do not resolve the issues.
- can see my list of unresolved questions and the number of unread potential answers received, so I don’t have to scan unrelated messages.
- can see a list of all unresolved questions and a list of the current potential answers for each so I can evaluate the potential answers and, if appropriate, propose a new potential answer without duplicating the work of others.
- can see a list of questions others have asked that might be related to a question I am about to ask, so I do not waste my time waiting for answers already there, and others don’t waste their time reading and answering my question.
- can search to find answered questions, currently unanswered questions, and reviewers so I can more quickly get my questions answered and create an effective set of reviewers to curate the results of my searches

### Functions added for New User Role (Phase 1 Work)
- Able to be assigned roles by admin (either through invitation code which dictates what their starting roles are or through dropdwon button in admin home page to change roles of existing accounts)
  - Added through InvitationPage.java file for the 1st method and AdminHomePage.java file for the 2nd method
- Can immediately go to home page after logging in (if have one role) or select a role before going to homep page (if more than 1 roles)
  - Added through RoleSelectionPage.java file (and redirects users based on which role they select)
- Ability to logout
  - Added function into both AdminHomePage.java and UserHomePage files
- Ability to log in to account once account is set up
  - Added through SetupAccountPage.java file (redirected users to WelcomeLoginPage)

### Functions added for Admin Role (Phase 1 Work)
- Able to provide one-time password for all users (can only be used once before reverting password back to user's original password)
  - Added through OneTimePassword.java file
- Able to view all accounts and their username, roles, and email as well as delete accounts from the system
  - Added through ViewAccountPage.java file
- Able to add/remove all roles (except for admin) from all account (is also the 2nd method of assigning roles to New Users)
  - Added through AdminHomePage.java file
