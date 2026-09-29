-- The backend uploads with the authenticated sitter's Supabase JWT.
-- Allow each user to create files only below uploads/<their auth uid>/...
-- The bucket is public, so no SELECT policy is required for public image URLs.

drop policy if exists "Sitter uploads own media" on storage.objects;

create policy "Sitter uploads own media"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'uploads'
  and (storage.foldername(name))[1] = (select auth.uid())::text
);
