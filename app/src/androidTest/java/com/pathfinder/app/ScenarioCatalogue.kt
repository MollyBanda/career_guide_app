package com.pathfinder.app

import com.pathfinder.app.data.model.OpportunityEntity

internal fun scenarioCatalogue() = listOf(
    OpportunityEntity(
        id = "test-career-software",
        title = "Software Engineer",
        category = "Careers",
        subtitle = "Technology · Software systems",
        overview = "Builds and tests software applications.",
        qualifications = "Computer science or related qualification.",
        courses = "Programming and software testing.",
        skills = "Problem solving and teamwork.",
        pathways = "Junior developer to senior engineer."
    ),
    OpportunityEntity(
        id = "test-career-ux",
        title = "UX Designer",
        category = "Careers",
        subtitle = "Design · Human-computer interaction",
        overview = "Designs accessible digital experiences.",
        qualifications = "Design or computing study; a portfolio helps."
    ),
    OpportunityEntity(
        id = "test-degree-cs",
        title = "Bachelor of Science in Computer Science",
        category = "Universities",
        subtitle = "University of Malawi · 3–4 years",
        overview = "A computing degree.",
        requirements = "Six credits including Mathematics, English, and Physics."
    ),
    OpportunityEntity(
        id = "test-degree-no-requirements",
        title = "Sample Computing Programme Without Requirements",
        category = "Universities",
        subtitle = "Sample institution · Programme details",
        overview = "A programme fixture with no recorded entry requirements."
    ),
    OpportunityEntity(
        id = "test-university-malawi",
        title = "University of Malawi",
        category = "Universities",
        subtitle = "Malawi · Computing and science programmes",
        overview = "Explore programmes and admissions.",
        provider = "University of Malawi"
    ),
    OpportunityEntity(
        id = "test-scholarship",
        title = "Commonwealth Master's Scholarship",
        category = "Scholarships",
        subtitle = "Postgraduate study · Eligibility varies by country",
        overview = "Scholarship details for postgraduate study.",
        requirements = "Check current eligibility with the provider.",
        provider = "Commonwealth Scholarship Commission",
        deadline = "Check current application cycle"
    ),
    OpportunityEntity(
        id = "test-scholarship-blank",
        title = "Sample Scholarship Without Details",
        category = "Scholarships",
        subtitle = "Sample listing",
        overview = "A test listing with no optional details."
    ),
    OpportunityEntity(
        id = "test-training",
        title = "Career Skills Bootcamp",
        category = "Training",
        subtitle = "Build practical skills · 6 weeks",
        overview = "A career readiness programme.",
        duration = "6 weeks"
    ),
    OpportunityEntity(
        id = "test-internship",
        title = "Clean Energy Intern",
        category = "Internships",
        subtitle = "Engineering · 12 weeks",
        overview = "A sample engineering placement.",
        duration = "12 weeks"
    )
)
