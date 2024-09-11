<div class="modal fade" id="modal_bug_report">
    <div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
        <div class="modal-content">

            <!-- Modal Header -->
            <div class="modal-header">
                <h4 class="modal-title">3D view</h4>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <span> 
                <form action="mailto:valentin.gauthier@geosiris.com" method="get" enctype="text/plain">
                    <input name="subject" type="text" class="form-control" value="[Webstudio] Bug report" hidden>
                    <div class="form-group">
                        <label for="modal_bug_report_name">Your name</label>
                        <input name="name" type="text" class="form-control" id="modal_bug_report_name" placeholder="Enter your name">
                    </div>
                    <div class="form-group">
                        <label for="modal_bug_report_mail">Your mail</label>
                        <input name="email" type="text" class="form-control" id="modal_bug_report_mail" placeholder="Enter your email">
                    </div>
                    <div class="form-group">
                        <label for="modal_bug_report_body">Message</label>
                        <textarea name="body" class="form-control" id="modal_bug_report_body" rows="20" placeholder="Describe the problem" aria-describedby="modal_bug_report_body_help"></textarea>
                        <small id="modal_bug_report_body_help" class="form-text text-muted">Do not share personal information, but if possible paste an xml/json representation of the objects related to the error.</small>
                    </div>
                    <input type="submit" name="submit" value="Submit" class="btn btn-primary">
                </form>
            </div>
        </div>
    </div>
</div>